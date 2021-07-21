package lists;

import classes.FilteredTransactions2;
import classes.FilteredTransactions2Request;
import classes.IdGenerator;
import d3e.core.CurrentUser;
import d3e.core.ListExt;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
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
public class FilteredTransactions2ChangeTracker implements Cancellable {
  private class FilteredTransactions2Data {
    long id;
    long a__customer_id_Rows;

    public FilteredTransactions2Data(long id, long a__customer_id_Rows) {
      this.id = id;
      this.a__customer_id_Rows = a__customer_id_Rows;
    }
  }
  private long id;
  
  private List<FilteredTransactions2Data> data;
  private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;
  private List<Disposable> disposables = ListExt.List();
  private FilteredTransactions2Request inputs;
  
  @Autowired private FilteredTransactions2Impl filteredTransactions2Impl;
  @Autowired private TransactionRepository transactionRepository;
  
  private class DQ1Cache {
    long transaction;
    long customer;
    
    DQ1Cache(long txn, long cus) {
      // TODO Auto-generated constructor stub
      this.transaction = txn;
      this.customer = cus;
    }
  }
  
  private List<DQ1Cache> dq1Cache = ListExt.List();
  
  public void getDQ1() {
    String sql =
        "select a._id a0, a._customer_id a1 from _transaction a where a._amount >= :param_0";
    List<Transaction> cache = filteredTransactions2Impl.getRows(sql, () -> ListExt.asList("param_0"), () -> ListExt.asList(inputs.amount), 0, Transaction.class);
    dq1Cache = cache.stream().map(x -> new DQ1Cache(x.getId(), x.getCustomer().getId())).collect(Collectors.toList());
  }

  public void init(
      ChangesConsumer changesConsumer,
      DataChangeTracker tracker,
      FilteredTransactions2 initialData,
      FilteredTransactions2Request inputs) {
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

  private void storeInitialData(FilteredTransactions2 initialData) {
    this.data =
        initialData.items.stream()
            .map((x) -> new FilteredTransactions2Data(x.getId(), x.getCustomer().getId()))
            .collect(Collectors.toList());
    long id = IdGenerator.getNext();
    this.id = id;
    initialData.id = id;
    
    getDQ1();
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
              List<FilteredTransactions2Data> existing =
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

  private FilteredTransactions2Data find(long id) {
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
        createUpdateChange(model);
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
    return model.getAmount() >= inputs.amount && model.getCustomer().getAge() >= 55l;
  }

  private void createInsertChange(Transaction model) {
    data.add(new FilteredTransactions2Data(model.getId(), model.getCustomer().getId()));
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
    FilteredTransactions2Data existing = find(id);
    if (existing == null) {
      return;
    }
    data.remove(existing);
    ListChange delete = new ListChange(this.id, -1, -1, ListChangeType.Removed, model);
    changesConsumer.writeListChange(delete);
  }

  private void applyWhereCustomer(long id, Customer customer) {
    /*
    TODO: Extract only relevant part of expression
    */
    boolean matched = base.getDouble(0) >= inputs.amount && customer.getAge() >= 55l;
    if (!(matched)) {
      /*
      TODO: Get from repository and create delete change
      */
    }
  }
}
