package lists;

import classes.IdGenerator;
import classes.StartFilteredTransactions;
import classes.StartFilteredTransactionsRequest;
import d3e.core.ListExt;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import models.Customer;
import models.Transaction;
import repository.jpa.TransactionRepository;
import rest.ws.Template;
import store.StoreEventType;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class StartFilteredTransactionsChangeTracker implements Cancellable {
  private long id;
  private List<StartFilteredTransactionsData> data;
  private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;
  private List<Disposable> disposables = ListExt.List();
  private StartFilteredTransactionsRequest inputs;
  
  @Autowired private TransactionRepository transactionRepository;
  
  private class StartFilteredTransactionsData {
    long id;
    long customer;
    
    public StartFilteredTransactionsData(long id, long customer) {
      // TODO Auto-generated constructor stub
      this.id = id;
      this.customer = customer;
    }
  }

  public StartFilteredTransactionsChangeTracker(
      ChangesConsumer changesConsumer,
      DataChangeTracker tracker,
      StartFilteredTransactions initialData) {
    this.changesConsumer = changesConsumer;
    this.tracker = tracker;
    storeInitialData(initialData);
    addSubscriptions();
  }

  @Override
  public void cancel() {
    disposables.forEach((d) -> d.dispose());
  }

  private void storeInitialData(StartFilteredTransactions initialData) {
    this.data = initialData.items.stream().map((x) -> new StartFilteredTransactionsData(x.getId(), x.getCustomer().getId())).collect(Collectors.toList());
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
    
    tracker.listen(1, null, (obj, type) -> {
      if (type != StoreEventType.Update) {
        return;
      }
      Customer model = (Customer) obj;
      long id = model.getId();
      List<StartFilteredTransactionsData> existing = this.data.stream().filter(x -> x.customer == id).collect(Collectors.toList());
      if (existing.isEmpty()) {
        // TODO: Caching
        return;
      }
      existing.forEach(x -> checkCustomerWhere(x.id, model));
    });
  }
  
  private void checkCustomerWhere(long id, Customer model) {
    boolean valid = model.getAge() >= 55;
    if (!valid) {
      Transaction one = transactionRepository.getOne(id);
      createDeleteChange(one);
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
    data.add(new StartFilteredTransactionsData(model.getId(), model.getCustomer().getId()));
    ListChange insert = new ListChange(this.id, -1, -1, ListChangeType.Added, model);
    changesConsumer.writeListChange(insert);
  }
  
  private List<StartFilteredTransactionsData> find(long id) {
    // TODO: Maybe remove
    return this.data.stream().filter(x -> x.id == id).collect(Collectors.toList());
  }
  
  private boolean has(long id) {
    return this.data.stream().anyMatch(x -> x.id == id);
  }

  private void createUpdateChange(Transaction model) {
    long id = model.getId();
    if (!has(id)) {
      return;
    }
    ListChange update = new ListChange(this.id, -1, -1, ListChangeType.Changed, model);
    changesConsumer.writeListChange(update);
  }

  private void createDeleteChange(Transaction model) {
    long id = model.getId();
    if (!has(id)) {
      return;
    }
    data.removeIf(x -> x.id == id);
    ListChange delete = new ListChange(this.id, -1, -1, ListChangeType.Removed, model);
    changesConsumer.writeListChange(delete);
  }
}
