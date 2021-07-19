package lists;

import java.util.List;
import java.util.stream.Collectors;

import classes.AllTransactions;
import classes.IdGenerator;
import d3e.core.ListExt;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import models.Transaction;
import rest.ws.Template;
import store.StoreEventType;

public class AllTransactionsChangeTracker implements Cancellable {
  private long id;
  private List<Long> data;
  private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;
  private List<Disposable> disposables = ListExt.List();
  
  public AllTransactionsChangeTracker(ChangesConsumer changesConsumer, DataChangeTracker tracker, AllTransactions initialData) {
    // TODO Auto-generated constructor stub
    this.changesConsumer = changesConsumer;
    this.tracker = tracker;
    storeInitialData(initialData);
    addSubscriptions();
  }
  
  @Override
  public void cancel() {
    // Maybe call this method to cancel the subscription?
    disposables.forEach((d) -> d.dispose());
  }

  private void storeInitialData(AllTransactions initialData) {
    this.data = initialData.items.stream().map(x -> x.getId()).collect(Collectors.toList());
    long id = IdGenerator.getNext();
    this.id = id;
    initialData.id = id;
  }

  private void addSubscriptions() {
    /*
    This method will register listeners on each reference that is referred to in the DataQuery expression.
    A listener is added by default on the Table from which we pull the data, since any change in that must trigger a subscription change.
    */
    Disposable baseSubscribe = tracker.listen(0, null, (obj, type) -> applyTransaction((Transaction) obj, type));
    disposables.add(baseSubscribe);
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
      createUpdateChange(model);
    }
  }

  private void createInsertChange(Transaction model) {
    data.add(model.getId());
    ListChange ins = new ListChange(this.id, -1, -1, ListChangeType.Added, model);
    changesConsumer.writeListChange(ins);
  }

  private void createUpdateChange(Transaction model) {
    long id = model.getId();
    if (!data.contains(id)) {
      return;
    }
    ListChange upd = new ListChange(this.id, -1, -1, ListChangeType.Changed, model);
    changesConsumer.writeListChange(upd);
  }

  private void createDeleteChange(Transaction model) {
    long id = model.getId();
    if (!data.contains(id)) {
      return;
    }
    data.remove(id);
    ListChange del = new ListChange(this.id, -1, -1, ListChangeType.Removed, model);
    changesConsumer.writeListChange(del);
  }
}