package lists;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import classes.IdGenerator;
import classes.OrderedTransactions;
import d3e.core.ListExt;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import models.Transaction;
import rest.ws.Template;
import store.StoreEventType;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OrderedTransactionsSubscriptionHelper2 implements Cancellable {
  private long id;
  private List<Long> data;
  private List<Double> orderBy;
  @Autowired private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;
  private List<Disposable> disposables = ListExt.List();
  
  public OrderedTransactionsSubscriptionHelper2(
      ChangesConsumer changesConsumer, DataChangeTracker tracker, OrderedTransactions initialData) {
    this.changesConsumer = changesConsumer;
    this.tracker = tracker;
    storeInitialData(initialData);
    addSubscriptions();
  }
  
  @Override
  public void cancel() throws Throwable {
    // TODO Auto-generated method stub
    disposables.forEach((d) -> d.dispose());
  }

  private void storeInitialData(OrderedTransactions initialData) {
    this.data = initialData.items.stream().map(x -> x.getId()).collect(Collectors.toList());
    this.orderBy = initialData.items.stream().map(x -> x.getAmount()).collect(Collectors.toList());
    long id = IdGenerator.getNext();
    this.id = id;
    initialData.id = id;
  }

  private void addSubscriptions() {
    /*
    This method will register listeners on each reference that is referred to in the DataQuery expression.
    A listener is added by default on the Table from which we pull the data, since any change in that must trigger a subscription change.
    */
    Disposable baseSubscribe = tracker.listen(id, null, (obj, type) -> applyTransaction((Transaction) obj, type));
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
      if (!(createPathChangeChange(model, old))) {
        createUpdateChange(model);
      }
    }
  }

  private void createInsertChange(Transaction model) {
    int index = (int) this.orderBy.stream().filter(x -> x <= model.getAmount()).count();
    data.add(index, model.getId());
    // TODO: Add index
    ListChange ins = createListChange(model);
    changesConsumer.writeListChange(ins);
  }
  
  private ListChange createListChange(Transaction model) {
    ListChange ins = new ListChange();
    ins.id = this.id;
    ins.type = -1;  // AllTransactions type
    ins.field = -1; // items field
    // TODO: Add change type
    ins.obj = model;
    return ins;
  }

  private void createUpdateChange(Transaction model) {
    long id = model.getId();
    if (!data.contains(id)) {
      return;
    }
    ListChange upd = createListChange(model);
    changesConsumer.writeListChange(upd);
  }

  private void createDeleteChange(Transaction model) {
    long id = model.getId();
    if (!data.contains(id)) {
      return;
    }
    int index = data.indexOf(id);
    data.remove(index);
    orderBy.remove(index);
    ListChange del = createListChange(model);
    changesConsumer.writeListChange(del);
  }

  private boolean createPathChangeChange(
      Transaction model, Transaction old) {
    boolean changed = old.getAmount() != model.getAmount();
    if (!(changed)) {
      return false;
    }
    long id = model.getId();
    if (!data.contains(id)) {
      return false;
    }
    long index = this.data.indexOf(id);
    double _orderBy0 = model.getAmount();
    long newIndex = this.orderBy.stream().filter(x -> x <= _orderBy0).count();
    Collections.swap(data, (int) index, (int) newIndex);
    Collections.swap(orderBy, (int) index, (int) newIndex);
    ListChange change = createPathChangeChange(index, newIndex);
    this.changesConsumer.writeListChange(change);
    return true;
  }

  private ListChange createPathChangeChange(long oldIndex, long index) {
    ListChange ins = new ListChange();
    ins.id = this.id;
    ins.type = -1;  // AllTransactions type
    ins.field = -1; // items field
    // TODO: Add change type
    ins.changeFromIndex = (int) oldIndex;
    ins.changeToIndex = (int) index;
    return ins;
  }
}
