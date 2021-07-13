package lists;

import classes.SubscriptionChangeType;
import d3e.core.CurrentUser;
import d3e.core.D3ESubscription;
import d3e.core.D3ESubscriptionEvent;
import d3e.core.ListExt;
import d3e.core.MapExt;
import d3e.core.TransactionWrapper;
import graphql.language.Field;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import models.Transaction;
import models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import store.StoreEventType;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OrderedTransactionsSubscriptionHelper
    implements FlowableOnSubscribe<DataQueryDataChange> {
  private class Output {
    Map<Long, Row> rows = MapExt.Map();
    List<OrderBy> orderByList = ListExt.List();

    public Output(List<NativeObj> rows) {
      for (int i = 0; i < rows.size(); i++) {
        NativeObj wrappedBase = rows.get(i);
        Row row = new Row("" + i, wrappedBase, i);
        this.rows.put(wrappedBase.getId(), row);
        NativeObj base = wrappedBase.getRef(1);
        double _orderBy0 = wrappedBase.getDouble(0);
        this.orderByList.add(new OrderBy(_orderBy0, row));
      }
    }

    public Row get(long id) {
      return rows.get(id);
    }

    public void insertRow(long id, Row row) {
      int insertAt = row.index;
      this.rows.values().stream()
          .filter((one) -> one.index >= insertAt)
          .forEach((one) -> one.index++);
      this.rows.put(id, row);
      NativeObj wrappedBase = row.row;
      NativeObj base = wrappedBase.getRef(1);
      double _orderBy0 = wrappedBase.getDouble(0);
      this.orderByList.add(new OrderBy(_orderBy0, row));
    }

    public Row deleteRow(long id) {
      Row row = this.rows.get(id);
      int deleteAt = row.index;
      this.rows.values().stream()
          .filter((one) -> one.index > deleteAt)
          .forEach((one) -> one.index--);
      ListExt.removeWhere(this.orderByList, (o) -> Objects.equals(o.row, row));
      return this.rows.remove(id);
    }

    public long getPath(double _orderBy0) {
      long index = 0;
      for (OrderBy orderBy : this.orderByList) {
        if (!(orderBy.fallsBefore(_orderBy0))) {
          break;
        }
        index++;
      }
      return index;
    }

    public void moveRow(Row row, int newIndex) {
      int oldIndex = row.index;
      this.rows.values().stream()
          .filter((one) -> one.index > oldIndex)
          .forEach((one) -> one.index--);
      this.rows.values().stream()
          .filter((one) -> one.index >= newIndex)
          .forEach((one) -> one.index++);
      row.index = newIndex;
    }
  }

  private class Row {
    String path;
    NativeObj row;
    int index;

    public Row(String path, NativeObj row, int index) {
      this.path = path;
      this.row = row;
      this.index = index;
    }
  }

  private class OrderBy {
    double _orderBy0;
    Row row;

    public OrderBy(double _orderBy0, Row row) {
      this._orderBy0 = _orderBy0;
      this.row = row;
    }

    public boolean fallsBefore(double _orderBy0) {
      if (this._orderBy0 < _orderBy0) {
        return true;
      }
      if (this._orderBy0 > _orderBy0) {
        return false;
      }
      return true;
    }

    public void update(double _orderBy0) {
      this._orderBy0 = _orderBy0;
    }
  }

  @Autowired private TransactionWrapper transactional;
  @Autowired private OrderedTransactionsImpl orderedTransactionsImpl;
  @Autowired private D3ESubscription subscription;
  private Flowable<DataQueryDataChange> flowable;
  private FlowableEmitter<DataQueryDataChange> emitter;
  private List<Disposable> disposables = ListExt.List();
  private Output output;
  private Field field;

  @Async
  public void handleContextStart(DataQueryDataChange event) {
    updateData(event);
    try {
      event.data = orderedTransactionsImpl.getAsJson(field, event.nativeData);
      event.nativeData = null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    this.emitter.onNext(event);
  }

  @Override
  public void subscribe(FlowableEmitter<DataQueryDataChange> emitter) throws Throwable {
    this.emitter = emitter;
    transactional.doInTransaction(this::init);
  }

  private void loadInitialData() {
    DataQueryDataChange change = new DataQueryDataChange();
    change.changeType = SubscriptionChangeType.All;
    change.nativeData = orderedTransactionsImpl.getNativeResult();
    handleContextStart(change);
  }

  private void init() {
    loadInitialData();
    addSubscriptions();
    emitter.setCancellable(() -> disposables.forEach((d) -> d.dispose()));
  }

  public Flowable<DataQueryDataChange> subscribe(Field field) {
    {
      User currentUser = CurrentUser.get();
    }
    this.field = field;
    this.flowable = Flowable.create(this, BackpressureStrategy.BUFFER);
    return this.flowable;
  }

  private void addSubscriptions() {
    /*
    This method will register listeners on each reference that is referred to in the DataQuery expression.
    A listener is added by default on the Table from which we pull the data, since any change in that must trigger a subscription change.
    */
    Disposable baseSubscribe =
        ((Flowable<D3ESubscriptionEvent<Transaction>>) subscription.onTransactionChangeEvent())
            .subscribe((e) -> applyTransaction(e));
    disposables.add(baseSubscribe);
  }

  public void applyTransaction(D3ESubscriptionEvent<Transaction> e) {
    List<DataQueryDataChange> changes = ListExt.List();
    Transaction model = e.model;
    StoreEventType type = e.changeType;
    if (type == StoreEventType.Insert) {
      /*
      New data is inserted
      So we just insert the new data depending on the clauses.
      */
      createInsertChange(changes, model);
    } else if (type == StoreEventType.Delete) {
      /*
      Existing data is deleted
      */
      createDeleteChange(changes, model);
    } else {
      /*
      Existing data is updated
      */
      Transaction old = model.getOld();
      if (old == null) {
        return;
      }
      if (!(createPathChangeChange(changes, model, old))) {
        createUpdateChange(changes, model);
      }
    }
    pushChanges(changes);
  }

  private void createInsertChange(List<DataQueryDataChange> changes, Transaction model) {
    DataQueryDataChange change = new DataQueryDataChange();
    change.nativeData = createTransactionData(model);
    change.changeType = SubscriptionChangeType.Insert;
    long index = this.output.getPath(model.getAmount());
    change.path = index == output.rows.size() ? "-1" : Long.toString(index);
    change.index = output.rows.size();
    changes.add(change);
  }

  private void createUpdateChange(List<DataQueryDataChange> changes, Transaction model) {
    Row row = output.get(model.getId());
    if (row == null) {
      return;
    }
    DataQueryDataChange change = new DataQueryDataChange();
    change.changeType = SubscriptionChangeType.Update;
    change.path = row.path;
    change.index = row.index;
    change.nativeData = ListExt.asList(row.row);
    changes.add(change);
  }

  private void createDeleteChange(List<DataQueryDataChange> changes, Transaction model) {
    Row row = output.get(model.getId());
    if (row == null) {
      return;
    }
    DataQueryDataChange change = new DataQueryDataChange();
    change.changeType = SubscriptionChangeType.Delete;
    change.path = row.path;
    change.index = row.index;
    change.nativeData = ListExt.asList(row.row);
    changes.add(change);
  }

  private boolean createPathChangeChange(
      List<DataQueryDataChange> changes, Transaction model, Transaction old) {
    boolean changed = old.getAmount() != model.getAmount();
    if (!(changed)) {
      return false;
    }
    Row row = output.get(model.getId());
    if (row == null) {
      return false;
    }
    double _orderBy0 = model.getAmount();
    long index = this.output.getPath(_orderBy0);
    createPathChangeChange(changes, row, index);
    this.output.orderByList.stream()
        .filter((one) -> one.row.equals(row))
        .forEach((one) -> one.update(_orderBy0));
    return true;
  }

  private void createPathChangeChange(List<DataQueryDataChange> changes, Row row, long index) {
    DataQueryDataChange change = new DataQueryDataChange();
    change.changeType = SubscriptionChangeType.PathChange;
    change.oldPath = row.path;
    change.index = ((int) index);
    change.path = Long.toString(index);
    change.nativeData = ListExt.asList(row.row);
    changes.add(change);
  }

  private List<NativeObj> createTransactionData(Transaction transaction) {
    List<NativeObj> data = ListExt.List();
    NativeObj row = new NativeObj(2);
    row.set(0, transaction.getAmount());
    row.set(1, transaction.getId());
    row.setId(1);
    data.add(row);
    return data;
  }

  private void pushChanges(List<DataQueryDataChange> changes) {
    changes.forEach(
        (one) -> {
          handleContextStart(one);
        });
  }

  private void updateData(DataQueryDataChange change) {
    switch (change.changeType) {
      case All:
        {
          this.output = new Output(change.nativeData);
          break;
        }
      case Delete:
        {
          NativeObj del = change.nativeData.get(0);
          output.deleteRow(del.getId());
          break;
        }
      case Insert:
        {
          NativeObj add = change.nativeData.get(0);
          String path = change.path.equals("-1") ? output.rows.size() + "" : change.path;
          Row newRow = new Row(path, add, change.index);
          output.insertRow(add.getId(), newRow);
          break;
        }
      case Update:
        {
          break;
        }
      case PathChange:
        {
          String oldPath = change.oldPath;
          String newPath = change.path;
          if (oldPath == null || newPath == null) {
            return;
          }
          NativeObj obj = change.nativeData.get(0);
          Row row = output.rows.get(obj.getId());
          if (!(Objects.equals(row.path, oldPath))) {
            return;
          }
          row.path = newPath;
          this.output.moveRow(row, change.index);
          break;
        }
      default:
        {
          break;
        }
    }
  }
}
