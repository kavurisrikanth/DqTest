package lists;

import classes.ConditionalOrderedTransactionsRequest;
import classes.ConditionalOrderedTransactionsSortBy;
import classes.IdGenerator;
import classes.OrderedTransactions;
import d3e.core.CurrentUser;
import d3e.core.ListExt;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import models.Transaction;
import models.User;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import rest.ws.Template;
import store.StoreEventType;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConditionalOrderedTransactionsChangeTracker implements Cancellable {
  private abstract class OrderBy {
    long id;
  }
  
  private class OrderByAmount extends OrderBy {
    double _orderBy0;
    
    public OrderByAmount(double _orderBy0, long id) {
      this._orderBy0 = _orderBy0;
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
  
  private class OrderByAge extends OrderBy {
    long _orderBy0;

    public OrderByAge(long _orderBy0, long id) {
      this._orderBy0 = _orderBy0;
      this.id = id;
    }
    
    public boolean fallsBefore(long _orderBy0) {
      if (this._orderBy0 < _orderBy0) {
        return true;
      }
      if (this._orderBy0 > _orderBy0) {
        return false;
      }        
      return true;
    }

    public void update(long _orderBy0) {
      this._orderBy0 = _orderBy0;
    }
  }

  private long id;
  private List<Long> data;
  private List<OrderBy> orderBy = ListExt.List();
  private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;
  private List<Disposable> disposables = ListExt.List();
  
  private ConditionalOrderedTransactionsRequest inputs;

  public void init(
      ChangesConsumer changesConsumer, DataChangeTracker tracker, OrderedTransactions initialData, ConditionalOrderedTransactionsRequest inputs) {
    {
      User currentUser = CurrentUser.get();
    }
    this.changesConsumer = changesConsumer;
    this.tracker = tracker;
    this.inputs = inputs;
    storeInitialData(initialData);
    addSubscriptions();
  }

  @Override
  public void cancel() {
    disposables.forEach((d) -> d.dispose());
  }

  private void storeInitialData(OrderedTransactions initialData) {
    this.data = initialData.items.stream().map((x) -> x.getId()).collect(Collectors.toList());
    this.orderBy =
        initialData.items.stream()
            .map((x) -> {
              if (this.inputs.sortBy == ConditionalOrderedTransactionsSortBy.AMOUNT) {
                return new OrderByAmount(x.getAmount(), x.getId());
              }
              if (this.inputs.sortBy == ConditionalOrderedTransactionsSortBy.AGE) {
                return new OrderByAge(x.getCustomer().getAge(), x.getId());
              }
              return new OrderByAmount(x.getAmount(), x.getId());
            })
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

  private Long find(long id) {
    /*
    TODO: Maybe remove
    */
    return this.data.stream().filter((x) -> x == id).findFirst().orElse(null);
  }

  private boolean has(long id) {
    return this.data.stream().anyMatch((x) -> x == id);
  }

  public void applyTransaction(Transaction model, StoreEventType type) {
    if (type == StoreEventType.Insert) {
      /*
      New data is inserted
      So we just insert the new data depending on the clauses.
      */
      createInsertChange(model);
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
      if (!(createPathChangeChange(model, old))) {
        createUpdateChange(model);
      }
    }
  }

  private void createInsertChange(Transaction model) {
    if (this.inputs.sortBy == ConditionalOrderedTransactionsSortBy.AMOUNT) {
      insertInAmountOrder(model);
    } else {
      // if (this.inputs.sortBy == ConditionalOrderedTransactionsSortBy.AGE)
      insertInAgeOrder(model);
    }
    ListChange insert = new ListChange(this.id, -1, -1, ListChangeType.Added, model);
    changesConsumer.writeListChange(insert);
  }
  
  private void insertInAmountOrder(Transaction model) {
    long id = model.getId();
    double _orderBy0 = model.getAmount();
    long index = 0;
    int orderBySize = this.orderBy.size();
    while (index < orderBySize && ((OrderByAmount) this.orderBy.get(((int) index))).fallsBefore(_orderBy0)) {
      index++;
    }
    data.add(((int) index), model.getId());
    this.orderBy.add(((int) index), new OrderByAmount(_orderBy0, id));
  }
  
  private void insertInAgeOrder(Transaction model) {
    long id = model.getId();
    long _orderBy0 = model.getCustomer().getAge();
    long index = 0;
    int orderBySize = this.orderBy.size();
    while (index < orderBySize && ((OrderByAge) this.orderBy.get(((int) index))).fallsBefore(_orderBy0)) {
      index++;
    }
    data.add(((int) index), model.getId());
    this.orderBy.add(((int) index), new OrderByAge(_orderBy0, id));
  }

  private void createUpdateChange(Transaction model) {
    long id = model.getId();
    if (!(has(id))) {
      return;
    }
    ListChange update = new ListChange(this.id, -1, -1, ListChangeType.Changed, model);
    changesConsumer.writeListChange(update);
  }

  private void createDeleteChange(Transaction model) {
    long id = model.getId();
    if (!(has(id))) {
      return;
    }
    data.remove(id);
    ListExt.removeWhere(this.orderBy, (x) -> x.id == id);
    ListChange delete = new ListChange(this.id, -1, -1, ListChangeType.Removed, model);
    changesConsumer.writeListChange(delete);
  }

  private boolean createPathChangeChange(Transaction model, Transaction old) {
    boolean changed = old.getAmount() != model.getAmount();
    if (!(changed)) {
      return false;
    }
    long id = model.getId();
    if (!(data.contains(id))) {
      return false;
    }
    int index = this.data.indexOf(id);
    long newIndex;
    if (this.inputs.sortBy == ConditionalOrderedTransactionsSortBy.AMOUNT) {
      newIndex = changePathInAmountOrder(model, index);
    } else {
      // if (this.inputs.sortBy == ConditionalOrderedTransactionsSortBy.AGE)
      newIndex = changePathInAgeOrder(model, index);
    }
    ListChange change =
        ListChange.forPathChange(
            id, -1, -1, ListChangeType.Changed, ((int) index), ((int) newIndex));
    this.changesConsumer.writeListChange(change);
    return true;
  }
  
  private long changePathInAmountOrder(Transaction model, int index) {
    double _orderBy0 = model.getAmount();
    long newIndex = 0;
    int orderBySize = this.orderBy.size();
    while (newIndex < orderBySize && ((OrderByAmount) this.orderBy.get(((int) newIndex))).fallsBefore(_orderBy0)) {
      newIndex++;
    }
    Collections.swap(data, index, ((int) newIndex));
    Collections.swap(this.orderBy, index, ((int) newIndex));
    ((OrderByAmount) this.orderBy.get(((int) newIndex))).update(_orderBy0);
    return newIndex;
  }
  
  private long changePathInAgeOrder(Transaction model, int index) {
    long _orderBy0 = model.getCustomer().getAge();
    long newIndex = 0;
    int orderBySize = this.orderBy.size();
    while (newIndex < orderBySize && ((OrderByAge) this.orderBy.get(((int) newIndex))).fallsBefore(_orderBy0)) {
      newIndex++;
    }
    Collections.swap(data, index, ((int) newIndex));
    Collections.swap(this.orderBy, index, ((int) newIndex));
    ((OrderByAge) this.orderBy.get(((int) newIndex))).update(_orderBy0);
    return newIndex;
  }
}
