package lists;

import classes.FilteredTransactions;
import classes.FilteredTransactionsRequest;
import classes.IdGenerator;
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
import java.util.List;
import java.util.Map;
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
public class FilteredTransactionsSubscriptionHelper2
    implements FlowableOnSubscribe<DataQueryDataChange> {
  @Autowired private TransactionWrapper transactional;
  @Autowired private FilteredTransactionsImpl filteredTransactionsImpl;
  @Autowired private D3ESubscription subscription;
  private Flowable<DataQueryDataChange> flowable;
  private FlowableEmitter<DataQueryDataChange> emitter;
  private List<Disposable> disposables = ListExt.List();
  private Field field;
  private FilteredTransactionsRequest inputs;
  
  private long id;
  private List<Long> data;
  @Autowired private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;

  @Override
  public void subscribe(FlowableEmitter<DataQueryDataChange> emitter) throws Throwable {
    this.emitter = emitter;
    transactional.doInTransaction(this::init);
  }

  private void loadInitialData() {
    FilteredTransactions result = filteredTransactionsImpl.get(this.inputs);
    this.data = result.items.stream().map(x -> x.getId()).collect(Collectors.toList());
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

  public Flowable<DataQueryDataChange> subscribe(Field field, FilteredTransactionsRequest inputs) {
    {
      User currentUser = CurrentUser.get();
    }
    this.field = field;
    this.inputs = inputs;
    this.flowable = Flowable.create(this, BackpressureStrategy.BUFFER);
    return this.flowable;
  }

  private void addSubscriptions() {
    /*
    This method will register listeners on each reference that is referred to in the DataQuery expression.
    A listener is added by default on the Table from which we pull the data, since any change in that must trigger a subscription change.
    */
    // TODO: Figure out the fields BitSet. It should encapsulate all the fields of this type used in various clauses
    Disposable baseSubscribe = tracker.listen(0, null, (obj, type) -> applyTransaction((Transaction) obj, type));
    disposables.add(baseSubscribe);
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
      boolean oldMatch = applyWhere(old);
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
    return model.getAmount() >= inputs.amount;
  }

  private void createInsertChange(Transaction model) {
    data.add(model.getId());
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
    data.remove(id);
    ListChange del = createListChange(model);
    changesConsumer.writeListChange(del);
  }
}
