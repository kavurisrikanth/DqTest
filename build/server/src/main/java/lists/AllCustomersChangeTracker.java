package lists;

import classes.AllCustomers;
import classes.IdGenerator;
import d3e.core.ListExt;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import java.util.List;
import java.util.stream.Collectors;
import models.Customer;
import rest.ws.Template;
import store.StoreEventType;

public class AllCustomersChangeTracker implements Cancellable {
  private long id;
  private List<Long> data;
  private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;
  private List<Disposable> disposables = ListExt.List();

  public AllCustomersChangeTracker(
      ChangesConsumer changesConsumer, DataChangeTracker tracker, AllCustomers initialData) {
    this.changesConsumer = changesConsumer;
    this.tracker = tracker;
    storeInitialData(initialData);
    addSubscriptions();
  }

  @Override
  public void cancel() {
    disposables.forEach((d) -> d.dispose());
  }

  private void storeInitialData(AllCustomers initialData) {
    this.data = initialData.items.stream().map((x) -> x.getId()).collect(Collectors.toList());
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
        tracker.listen(0, null, (obj, type) -> applyCustomer(((Customer) obj), type));
    disposables.add(baseSubscribe);
  }

  public void applyCustomer(Customer model, StoreEventType type) {
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
      Customer old = model.getOld();
      if (old == null) {
        return;
      }
      createUpdateChange(model);
    }
  }

  private void createInsertChange(Customer model) {
    data.add(model.getId());
    ListChange insert = new ListChange(this.id, -1, -1, ListChangeType.Added, model);
    changesConsumer.writeListChange(insert);
  }

  private void createUpdateChange(Customer model) {
    long id = model.getId();
    if (!(data.contains(id))) {
      return;
    }
    ListChange update = new ListChange(this.id, -1, -1, ListChangeType.Changed, model);
    changesConsumer.writeListChange(update);
  }

  private void createDeleteChange(Customer model) {
    long id = model.getId();
    if (!(data.contains(id))) {
      return;
    }
    data.remove(id);
    ListChange delete = new ListChange(this.id, -1, -1, ListChangeType.Removed, model);
    changesConsumer.writeListChange(delete);
  }

  private List<NativeObj> createCustomerData(Customer customer) {
    List<NativeObj> data = ListExt.List();
    NativeObj row = new NativeObj(customer.getId());
    data.add(row);
    return data;
  }
}
