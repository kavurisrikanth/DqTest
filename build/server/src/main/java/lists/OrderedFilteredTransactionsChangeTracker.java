package lists;

import classes.Gender;
import classes.IdGenerator;
import classes.OrderedFilteredTransactions;
import d3e.core.CurrentUser;
import d3e.core.ListExt;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import models.Customer;
import models.Transaction;
import models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import repository.jpa.TransactionRepository;
import rest.ws.Template;
import store.StoreEventType;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OrderedFilteredTransactionsChangeTracker implements Cancellable {
  private class OrderBy {
    double _orderBy0;
    long id;

    public OrderBy(double _orderBy0, long id) {
      this._orderBy0 = _orderBy0;
      this.id = id;
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

  private class OrderedFilteredTransactionsData {
    long id;
    long a__customer_id_Rows;

    public OrderedFilteredTransactionsData(long id, long a__customer_id_Rows) {
      this.id = id;
      this.a__customer_id_Rows = a__customer_id_Rows;
    }
  }

  private long id;
  private List<OrderedFilteredTransactionsData> data;
  private List<OrderBy> orderBy = ListExt.List();
  private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;
  private List<Disposable> disposables = ListExt.List();
  @Autowired private OrderedFilteredTransactionsImpl orderedFilteredTransactionsImpl;
  @Autowired private TransactionRepository transactionRepository;

  public void init(
      ChangesConsumer changesConsumer,
      DataChangeTracker tracker,
      OrderedFilteredTransactions initialData) {
    {
      User currentUser = CurrentUser.get();
    }
    this.changesConsumer = changesConsumer;
    this.tracker = tracker;
    storeInitialData(initialData);
    addSubscriptions();
  }

  @Override
  public void cancel() {
    disposables.forEach((d) -> d.dispose());
  }

  private void storeInitialData(OrderedFilteredTransactions initialData) {
    this.data =
        initialData.items.stream()
            .map((x) -> new OrderedFilteredTransactionsData(x.getId(), x.getCustomer().getId()))
            .collect(Collectors.toList());
    this.orderBy =
        initialData.items.stream()
            .map((x) -> new OrderBy(x.getAmount(), x.getId()))
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
    Disposable customerSubscribe =
        tracker.listen(
            0,
            null,
            (obj, type) -> {
              if (type != StoreEventType.Update) {
                return;
              }
              Customer model = ((Customer) obj);
              long id = model.getId();
              List<OrderedFilteredTransactionsData> existing =
                  this.data.stream()
                      .filter(
                          (x) -> {
                            /*
                            TODO
                            */
                            return x.a__customer_id_Rows == id;
                          })
                      .collect(Collectors.toList());
              if (existing.isEmpty()) {
                /*
                TODO: Caching
                */
              }
              existing.forEach(
                  (x) -> {
                    applyWhereCustomer(x.id, model);
                  });
            });
    disposables.add(customerSubscribe);
  }

  private OrderedFilteredTransactionsData find(long id) {
    /*
    TODO: Maybe remove
    */
    return this.data.stream().filter((x) -> x.id == id).findFirst().orElse(null);
  }

  private boolean has(long id) {
    return this.data.stream().anyMatch((x) -> x.id == id);
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
      boolean oldMatch = has(old.getId());
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
    long index = 0;
    int orderBySize = this.orderBy.size();
    while (index < orderBySize && this.orderBy.get(((int) index)).fallsBefore(_orderBy0)) {
      index++;
    }
    data.add(
        ((int) index),
        new OrderedFilteredTransactionsData(model.getId(), model.getCustomer().getId()));
    this.orderBy.add(((int) index), new OrderBy(_orderBy0, id));
    ListChange insert = new ListChange(this.id, -1, -1, ListChangeType.Added, model);
    changesConsumer.writeListChange(insert);
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
    OrderedFilteredTransactionsData existing = find(id);
    if (existing == null) {
      return;
    }
    data.remove(existing);
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
    double _orderBy0 = model.getAmount();
    long newIndex = 0;
    int orderBySize = this.orderBy.size();
    while (newIndex < orderBySize && this.orderBy.get(((int) newIndex)).fallsBefore(_orderBy0)) {
      newIndex++;
    }
    Collections.swap(data, index, ((int) newIndex));
    Collections.swap(this.orderBy, index, ((int) newIndex));
    this.orderBy.get(((int) newIndex)).update(_orderBy0);
    ListChange change =
        ListChange.forPathChange(
            id, -1, -1, ListChangeType.Changed, ((int) index), ((int) newIndex));
    this.changesConsumer.writeListChange(change);
    return true;
  }

  private void applyWhereCustomer(long id, Customer customer) {
    /*
    TODO: Extract only relevant part of expression
    */
    boolean matched = customer.getGender() == Gender.Female;
    if (!(matched)) {
      /*
      TODO: Get from repository and create delete change
      */
    }
  }
}
