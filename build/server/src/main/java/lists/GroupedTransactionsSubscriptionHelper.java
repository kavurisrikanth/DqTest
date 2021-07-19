package lists;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import classes.GroupedTransactions;
import classes.GroupedTransactionsRequest;
import classes.IdGenerator;
import d3e.core.D3ESubscription;
import d3e.core.ListExt;
import d3e.core.TransactionWrapper;
import graphql.language.Field;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.disposables.Disposable;
import models.Customer;
import models.Transaction;
import rest.ws.Template;
import store.StoreEventType;

public class GroupedTransactionsSubscriptionHelper {
  private class GTxnData {
    long id;
    long customer;
    List<Long> data;
    List<Double> order;
  }
  
  @Autowired private TransactionWrapper transactional;
  @Autowired private GroupedTransactionsImpl groupedTransactionsImpl;
  @Autowired private D3ESubscription subscription;
  private Flowable<DataQueryDataChange> flowable;
  private FlowableEmitter<DataQueryDataChange> emitter;
  private List<Disposable> disposables = ListExt.List();
  private Field field;
  private GroupedTransactionsRequest inputs;
  
  private long id;
  private List<GTxnData> data;
  @Autowired private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;
  
  private void init() {
    loadInitialData();
    addSubscriptions();
    emitter.setCancellable(() -> disposables.forEach((d) -> d.dispose()));
  }
  
  private void loadInitialData() {
    // TODO Auto-generated method stub
    GroupedTransactions result = groupedTransactionsImpl.get(this.inputs);
    this.data = result.items.stream().map(x -> {
      GTxnData data = new GTxnData();
      data.id = IdGenerator.getNext();
      data.customer = x.customer.getId();
      data.data = x.txns.stream().map(y -> y.getId()).collect(Collectors.toList());
      data.order = x.txns.stream().map(y -> y.getAmount()).collect(Collectors.toList());
      return data;
    }).collect(Collectors.toList());
    long id = IdGenerator.getNext();
    this.id = id;
    result.id = id;
    
    ObjectChange basic = new ObjectChange();
    basic.id = id;
    // TODO
    basic.type = 0;
    Change ch = new Change();
    ch.field = -1;  //items
    ch.value = basic.changes;
    basic.changes = ListExt.asList(ch);
    
    changesConsumer.writeObjectChange(basic);
  }

  private void addSubscriptions() {
    // TODO Auto-generated method stub
    Disposable baseSubscribe = tracker.listen(0, null, (obj, type) -> applyTransaction((Transaction) obj, type));
    disposables.add(baseSubscribe);
    
    Disposable groupSubscribe = tracker.listen(-1, null, (obj, type) -> {
      if (type != StoreEventType.Update) {
        return;
      }
      applyCustomer((Customer) obj, type);
    });
    disposables.add(groupSubscribe);
  }

  private void applyTransaction(Transaction model, StoreEventType type) {
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
      if (!createPathChangeChange(model, old)) {
        createUpdateChange(model);
      }
    }
  }
  
  private void createInsertChange(Transaction model) {
    GTxnData value = data.stream().filter(x -> x.customer == model.getCustomer().getId()).findFirst().orElse(null);
    if (value == null) {
      //Add new
    }
    value.data.add(model.getId());
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
  
  private boolean createPathChangeChange(Transaction model, Transaction old) {
    boolean changed = !Objects.equals(model.getCustomer(), old.getCustomer());
    if (!(changed)) {
      return false;
    }
    long id = model.getId();
    long oldId = old.getId();
    GTxnData oldGroup = this.data.stream().filter(x -> x.customer == oldId).findFirst().orElse(null);
    if (oldGroup == null) {
      // Data has changed from somewhere. This should not happen.
    }
    int oldIndex = oldGroup.data.indexOf(oldId);
    oldGroup.data.remove(oldIndex);
    oldGroup.order.remove(oldIndex);
    GTxnData currentGroup = this.data.stream().filter(x -> x.customer == id).findFirst().orElse(null);
    if (currentGroup == null) {
      // Create new
      currentGroup = new GTxnData();
      currentGroup.id = IdGenerator.getNext();
      currentGroup.customer = id;
    }
    double _orderBy0 = model.getAmount();
    long newIndex = currentGroup.order.stream().filter(x -> x <= _orderBy0).count();
    currentGroup.order.add((int) newIndex, _orderBy0);
    currentGroup.data.add((int) newIndex, id);
    ListChange change = createListChange(model);
    this.changesConsumer.writeListChange(change);
    return true;
  }

  private void createUpdateChange(Transaction model) {
    long id = model.getId();
    GTxnData currentGroup = this.data.stream().filter(x -> x.customer == id).findFirst().orElse(null);
    if (currentGroup == null) {
      return;
    }
    ListChange upd = createListChange(model);
    changesConsumer.writeListChange(upd);
  }

  private void createDeleteChange(Transaction model) {
    long id = model.getId();
    GTxnData currentGroup = this.data.stream().filter(x -> x.customer == id).findFirst().orElse(null);
    if (currentGroup == null) {
      return;
    }
    int index = currentGroup.data.indexOf(id);
    currentGroup.data.remove(index);
    currentGroup.order.remove(index);
    ListChange del = createListChange(model);
    changesConsumer.writeListChange(del);
  }
  
  private void applyCustomer(Customer model, StoreEventType type) {
    // TODO Auto-generated method stub
    // Only for update
    // Not applicable in this case, since id will not change.
    return;
  }
}
