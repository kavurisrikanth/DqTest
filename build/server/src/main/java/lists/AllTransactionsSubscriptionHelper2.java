package lists;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import classes.AllTransactions;
import classes.IdGenerator;
import d3e.core.CurrentUser;
import d3e.core.D3ESubscription;
import d3e.core.ListExt;
import d3e.core.TransactionWrapper;
import graphql.language.Field;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import models.Transaction;
import models.User;
import rest.ws.Template;
import store.StoreEventType;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AllTransactionsSubscriptionHelper2 implements FlowableOnSubscribe<DataQueryDataChange> {
  @Autowired private TransactionWrapper transactional;
  @Autowired private AllTransactionsImpl allTransactionsImpl;
  @Autowired private D3ESubscription subscription;
  private Flowable<DataQueryDataChange> flowable;
  private FlowableEmitter<DataQueryDataChange> emitter;
  private List<Disposable> disposables = ListExt.List();
  private Field field;

  private long id;
  private List<Long> data;
  @Autowired private DataChangeTracker tracker;
  private ChangesConsumer changesConsumer;
  private Template template;
  
  public AllTransactionsSubscriptionHelper2(ChangesConsumer changesConsumer) {
    // TODO Auto-generated constructor stub
    this.changesConsumer = changesConsumer;
  }

  @Override
  public void subscribe(FlowableEmitter<DataQueryDataChange> emitter) throws Throwable {
    this.emitter = emitter;
    transactional.doInTransaction(this::init);
  }

  private void loadInitialData() {
    AllTransactions result = allTransactionsImpl.get();
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
