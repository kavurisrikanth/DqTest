package lists;

import classes.AllTransactions;
import classes.IdGenerator;
import classes.OrderedTransactions;
import classes.SubscriptionChangeType;
import d3e.core.CurrentUser;
import d3e.core.D3ESubscription;
import d3e.core.D3ESubscriptionEvent;
import d3e.core.ListExt;
import d3e.core.MapExt;
import d3e.core.TransactionWrapper;
import graphql.language.Field;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import models.Transaction;
import models.User;
import rest.ws.Template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import store.StoreEventType;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OrderedTransactionsSubscriptionHelper2
    implements FlowableOnSubscribe<DataQueryDataChange> {
  @Autowired private TransactionWrapper transactional;
  @Autowired private OrderedTransactionsImpl orderedTransactionsImpl;
  @Autowired private D3ESubscription subscription;
  private Flowable<DataQueryDataChange> flowable;
  private FlowableEmitter<DataQueryDataChange> emitter;
  private List<Disposable> disposables = ListExt.List();
  private Field field;
  
  private long id;
  private List<Long> data;
  private List<Double> orderBy;
  @Autowired private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;

  @Override
  public void subscribe(FlowableEmitter<DataQueryDataChange> emitter) throws Throwable {
    this.emitter = emitter;
    transactional.doInTransaction(this::init);
  }

  private void loadInitialData() {
    OrderedTransactions result = orderedTransactionsImpl.get();
    this.data = result.items.stream().map(x -> x.getId()).collect(Collectors.toList());
    this.orderBy = result.items.stream().map(x -> x.getAmount()).collect(Collectors.toList());
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

  private void init() {
    loadInitialData();
    addSubscriptions();
    emitter.setCancellable(() -> disposables.forEach((d) -> d.dispose()));
  }

  public Flowable<DataQueryDataChange> subscribe(Field field) {
    {
      User currentUser = CurrentUser.get();
    }
    this.field = field;
    this.flowable = Flowable.create(this, BackpressureStrategy.BUFFER);
    return this.flowable;
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
