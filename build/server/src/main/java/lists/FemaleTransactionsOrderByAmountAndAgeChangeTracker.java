package lists;

import classes.FemaleTransactionsOrderByAmountAndAge;
import classes.Gender;
import classes.IdGenerator;
import d3e.core.ListExt;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import models.Customer;
import models.Transaction;
import rest.ws.Template;
import store.StoreEventType;

public class FemaleTransactionsOrderByAmountAndAgeChangeTracker implements Cancellable {
  private class OrderBy {
    double _orderBy0;
    long _orderBy1;
    long id;

    public OrderBy(double _orderBy0, long _orderBy1, long id) {
      this._orderBy0 = _orderBy0;
      this._orderBy1 = _orderBy1;
      this.id = id;
    }

    public boolean fallsBefore(double _orderBy0, long _orderBy1) {
      if (this._orderBy0 < _orderBy0) {
        return true;
      }
      if (this._orderBy0 > _orderBy0) {
        return false;
      }
      if (this._orderBy1 > _orderBy1) {
        return true;
      }
      if (this._orderBy1 < _orderBy1) {
        return false;
      }
      return true;
    }

    public void update(double _orderBy0, long _orderBy1) {
      this._orderBy0 = _orderBy0;
      this._orderBy1 = _orderBy1;
    }
  }

  private long id;
  private List<Long> data;
  private List<OrderBy> orderBy = ListExt.List();
  private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;
  private List<Disposable> disposables = ListExt.List();

  public FemaleTransactionsOrderByAmountAndAgeChangeTracker(
      ChangesConsumer changesConsumer,
      DataChangeTracker tracker,
      FemaleTransactionsOrderByAmountAndAge initialData) {
    this.changesConsumer = changesConsumer;
    this.tracker = tracker;
    storeInitialData(initialData);
    addSubscriptions();
  }

  @Override
  public void cancel() {
    disposables.forEach((d) -> d.dispose());
  }

  private void storeInitialData(FemaleTransactionsOrderByAmountAndAge initialData) {
    this.data = initialData.items.stream().map((x) -> x.getId()).collect(Collectors.toList());
    this.orderBy =
        initialData.items.stream()
            .map((x) -> new OrderBy(x.getAmount(), x.getCustomer().getAge(), x.getId()))
            .collect(Collectors.toList());
    long id = IdGenerator.getNext();
    this.id = id;
    initialData.id = id;
  }

  private void addSubscriptions() {
    /*
    This method will register listeners on each reference that is referred to in the DataQuery expression.
    A listener is added by default on the Table from which we pull the data, since any change in that must trigger a subscription change.
    */
    Disposable baseSubscribe =
        tracker.listen(0, null, (obj, type) -> applyTransaction(((Transaction) obj), type));
    disposables.add(baseSubscribe);
  }

  public void applyTransaction(Transaction model, StoreEventType type) {
    if (type == StoreEventType.Insert) {
      /*
      New data is inserted
      So we just insert the new data depending on the clauses.
      */
      if (applyWhere(model)) {
        createInsertChange(model);
      }
    } else if (type == StoreEventType.Delete) {
      /*
      Existing data is deleted
      */
      createDeleteChange(model);
    } else {
      /*
      Existing data is updated
      */
      Transaction old = model.getOld();
      if (old == null) {
        return;
      }
      boolean currentMatch = applyWhere(model);
      boolean oldMatch = this.data.contains(old.getId());
      if (currentMatch == oldMatch) {
        if (!(currentMatch) && !(oldMatch)) {
          return;
        }
        if (!(createPathChangeChange(model, old))) {
          createUpdateChange(model);
        }
      } else {
        if (oldMatch) {
          createDeleteChange(model);
        }
        if (currentMatch) {
          createInsertChange(model);
        }
      }
    }
  }

  private boolean applyWhere(Transaction model) {
    return model.getCustomer().getGender() == Gender.Female;
  }

  private void createInsertChange(Transaction model) {
    long id = model.getId();
    double _orderBy0 = model.getAmount();
    long _orderBy1 = model.getCustomer().getAge();
    long index = 0;
    int orderBySize = this.orderBy.size();
    while (index < orderBySize
        && this.orderBy.get(((int) index)).fallsBefore(_orderBy0, _orderBy1)) {
      index++;
    }
    data.add(((int) index), id);
    this.orderBy.add(((int) index), new OrderBy(_orderBy0, _orderBy1, id));
    ListChange insert = new ListChange(this.id, -1, -1, ListChangeType.Added, model);
    changesConsumer.writeListChange(insert);
  }

  private void createUpdateChange(Transaction model) {
    long id = model.getId();
    if (!(data.contains(id))) {
      return;
    }
    ListChange update = new ListChange(this.id, -1, -1, ListChangeType.Changed, model);
    changesConsumer.writeListChange(update);
  }

  private void createDeleteChange(Transaction model) {
    long id = model.getId();
    if (!(data.contains(id))) {
      return;
    }
    data.remove(id);
    ListExt.removeWhere(this.orderBy, (x) -> x.id == id);
    ListChange delete = new ListChange(this.id, -1, -1, ListChangeType.Removed, model);
    changesConsumer.writeListChange(delete);
  }

  private boolean createPathChangeChange(Transaction model, Transaction old) {
    boolean changed =
        old.getAmount() != model.getAmount()
            || old.getCustomer().getAge() != model.getCustomer().getAge();
    if (!(changed)) {
      return false;
    }
    long id = model.getId();
    if (!(data.contains(id))) {
      return false;
    }
    int index = this.data.indexOf(id);
    double _orderBy0 = model.getAmount();
    long _orderBy1 = model.getCustomer().getAge();
    long newIndex = 0;
    int orderBySize = this.orderBy.size();
    while (newIndex < orderBySize
        && this.orderBy.get(((int) newIndex)).fallsBefore(_orderBy0, _orderBy1)) {
      newIndex++;
    }
    Collections.swap(data, index, ((int) newIndex));
    Collections.swap(this.orderBy, index, ((int) newIndex));
    this.orderBy.get(((int) newIndex)).update(_orderBy0, _orderBy1);
    ListChange change =
        ListChange.forPathChange(
            id, -1, -1, ListChangeType.Changed, ((int) index), ((int) newIndex));
    this.changesConsumer.writeListChange(change);
    return true;
  }
}
