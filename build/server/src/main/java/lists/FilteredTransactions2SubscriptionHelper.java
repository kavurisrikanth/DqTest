package lists;

import classes.FilteredTransactions2Request;
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
import models.Customer;
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
public class FilteredTransactions2SubscriptionHelper
    implements FlowableOnSubscribe<DataQueryDataChange> {
  private class Output {
    Map<Long, Row> rows = MapExt.Map();

    public Output(List<NativeObj> rows) {
      for (int i = 0; i < rows.size(); i++) {
        NativeObj wrappedBase = rows.get(i);
        Row row = new Row("" + i, wrappedBase, i);
        this.rows.put(wrappedBase.getId(), row);
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
    }

    public Row deleteRow(long id) {
      Row row = this.rows.get(id);
      int deleteAt = row.index;
      this.rows.values().stream()
          .filter((one) -> one.index > deleteAt)
          .forEach((one) -> one.index--);
      return this.rows.remove(id);
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

  @Autowired private TransactionWrapper transactional;
  @Autowired private FilteredTransactions2Impl filteredTransactions2Impl;
  @Autowired private D3ESubscription subscription;
  private Flowable<DataQueryDataChange> flowable;
  private FlowableEmitter<DataQueryDataChange> emitter;
  private List<Disposable> disposables = ListExt.List();
  private Output output;
  private Field field;
  private FilteredTransactions2Request inputs;
  private Map<Long, List<Row>> a__customer_id_Rows = MapExt.Map();

  @Async
  public void handleContextStart(DataQueryDataChange event) {
    updateData(event);
    try {
      event.data = filteredTransactions2Impl.getAsJson(field, event.nativeData);
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
    change.nativeData = filteredTransactions2Impl.getNativeResult(this.inputs);
    handleContextStart(change);
  }

  private void init() {
    loadInitialData();
    addSubscriptions();
    emitter.setCancellable(() -> disposables.forEach((d) -> d.dispose()));
  }

  public Flowable<DataQueryDataChange> subscribe(Field field, FilteredTransactions2Request inputs) {
    {
      User currentUser = CurrentUser.get();
    }
    this.field = field;
    this.inputs = inputs;
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
    Disposable CustomerSubscribe =
        ((Flowable<D3ESubscriptionEvent<Customer>>) subscription.onCustomerChangeEvent())
            .subscribe(
                (e) -> {
                  if (e.changeType != StoreEventType.Update) {
                    return;
                  }
                  Customer value = e.model;
                  List<Row> row0 = a__customer_id_Rows.get(value.getId());
                  if (row0 == null) {
                    /*
                    TODO: Generate the proper condition here
                    */
                    if (false) {
                      loadInitialData();
                    }
                  } else {
                    row0.forEach(
                        (r) -> {
                          applyWhereCustomer(r, value);
                        });
                  }
                });
    disposables.add(CustomerSubscribe);
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
      if (applyWhere(model)) {
        createInsertChange(changes, model);
      }
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
      boolean currentMatch = applyWhere(model);
      boolean oldMatch = applyWhere(old);
      if (currentMatch == oldMatch) {
        if (!(currentMatch) && !(oldMatch)) {
          return;
        }
        createUpdateChange(changes, model);
      } else {
        if (oldMatch) {
          createDeleteChange(changes, model);
        }
        if (currentMatch) {
          createInsertChange(changes, model);
        }
      }
    }
    pushChanges(changes);
  }

  private boolean applyWhere(Transaction model) {
    return model.getAmount() >= inputs.amount && model.getCustomer().getAge() >= 55l;
  }

  private void createInsertChange(List<DataQueryDataChange> changes, Transaction model) {
    DataQueryDataChange change = new DataQueryDataChange();
    change.nativeData = createTransactionData(model);
    change.changeType = SubscriptionChangeType.Insert;
    change.path = "-1";
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
    createDeleteChange(changes, row);
  }

  private void createDeleteChange(List<DataQueryDataChange> changes, Row row) {
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

  private List<NativeObj> createTransactionData(Transaction transaction) {
    List<NativeObj> data = ListExt.List();
    NativeObj row = new NativeObj(4);
    row.set(0, transaction.getAmount());
    row.set(1, transaction.getCustomer().getAge());
    row.set(2, transaction.getCustomer().getId());
    row.set(3, transaction.getId());
    row.setId(3);
    data.add(row);
    return data;
  }

  private void pushChanges(List<DataQueryDataChange> changes) {
    changes.forEach(
        (one) -> {
          handleContextStart(one);
        });
  }

  private void updateData(NativeObj ref, Map<Long, List<Row>> rows, Row thisRow, boolean remove) {
    if (ref == null) {
      return;
    }
    List<Row> list = rows.get(ref.getId());
    if (list == null) {
      if (remove) {
        return;
      }
      list = ListExt.List();
      rows.put(ref.getId(), list);
    }
    if (remove) {
      list.remove(thisRow);
    } else {
      list.add(thisRow);
    }
  }

  private void updateData(DataQueryDataChange change) {
    switch (change.changeType) {
      case All:
        {
          this.output = new Output(change.nativeData);
          this.output
              .rows
              .values()
              .forEach(
                  (r) -> {
                    NativeObj wrappedBase = r.row;
                    if (wrappedBase != null) {
                      NativeObj ref0 = wrappedBase.getRef(2);
                      updateData(ref0, a__customer_id_Rows, r, false);
                    }
                  });
          break;
        }
      case Delete:
        {
          NativeObj del = change.nativeData.get(0);
          Row delRow = output.deleteRow(del.getId());
          NativeObj wrappedBase = delRow.row;
          if (wrappedBase != null) {
            NativeObj ref0 = wrappedBase.getRef(2);
            updateData(ref0, a__customer_id_Rows, delRow, true);
          }
          break;
        }
      case Insert:
        {
          NativeObj add = change.nativeData.get(0);
          String path = change.path.equals("-1") ? output.rows.size() + "" : change.path;
          Row newRow = new Row(path, add, change.index);
          output.insertRow(add.getId(), newRow);
          NativeObj wrappedBase = newRow.row;
          if (wrappedBase != null) {
            NativeObj ref0 = wrappedBase.getRef(2);
            updateData(ref0, a__customer_id_Rows, newRow, false);
          }
          break;
        }
      case Update:
        {
          break;
        }
      default:
        {
          break;
        }
    }
  }

  private void applyWhereCustomer(Row r, Customer customer) {
    NativeObj wrappedBase = r.row;
    NativeObj base = wrappedBase.getRef(3);
    boolean matched = base.getDouble(0) >= inputs.amount && customer.getAge() >= 55l;
    if (!(matched)) {
      List<DataQueryDataChange> changes = ListExt.List();
      createDeleteChange(changes, r);
      pushChanges(changes);
    }
  }
}
