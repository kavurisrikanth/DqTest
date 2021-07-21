package lists;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import classes.FilteredTransactions2;
import classes.FilteredTransactions2Request;
import classes.IdGenerator;
import d3e.core.ListExt;
import d3e.core.MapExt;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import models.Customer;
import models.Transaction;
import repository.jpa.TransactionRepository;
import rest.ws.Template;
import store.StoreEventType;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FilteredTransactions2ChangeTracker implements Cancellable {
  private long id;
  private List<Long> data;
  private Map<Long, List<Long>> refInfo = MapExt.Map();
  
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
      FilteredTransactions2 initialData) {
    this.changesConsumer = changesConsumer;
    this.tracker = tracker;
    storeInitialData(initialData);
    addSubscriptions();
  }

  @Override
  public void cancel() {
    disposables.forEach((d) -> d.dispose());
  }

  private void storeInitialData(FilteredTransactions2 initialData) {
    this.data = initialData.items.stream().map((x) -> x.getId()).collect(Collectors.toList());
    initialData.items.forEach(one -> {
      long id = one.getId();
      long customer_id = one.getCustomer().getId();
      this.refInfo.putIfAbsent(customer_id, ListExt.List());
      this.refInfo.get(customer_id).add(id);
    });
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
    
    // Customer listener
    tracker.listen(1, null, (obj, type) -> {
      if (type != StoreEventType.Update) {
        return;
      }
      Customer c = (Customer) obj;
      long id = c.getId();
      List<Long> found = this.refInfo.get(id);
      if (found.isEmpty()) {
        // Look in cache
        List<DQ1Cache> inCache = this.dq1Cache.stream().filter(x -> x.customer == id).collect(Collectors.toList());
        if (inCache.isEmpty()) {
          // Nothing to do - Maybe stale cache?
          return;
        }
        // Update refInfo (and then run this again?)
        List<Long> fromCache = inCache.stream().map(x -> x.transaction).collect(Collectors.toList());
        this.refInfo.put(id, fromCache);
        found = fromCache;
      }
      found.forEach(i -> checkCustomerWhere(i, c));
    });
  }

  private void checkCustomerWhere(long id, Customer c) {
    // TODO Auto-generated method stub
    
    // id is Transaction id
    
    // TODO: Extract only this part of the condition
    boolean valid = c.getAge() >= 55;
    if (!valid) {
      Transaction txn = transactionRepository.getOne(id);
      createDeleteChange(txn);
    }
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
    data.add(model.getId());
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
    ListChange delete = new ListChange(this.id, -1, -1, ListChangeType.Removed, model);
    changesConsumer.writeListChange(delete);
  }
}
