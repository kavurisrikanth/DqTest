package d3e.core;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import javax.annotation.PostConstruct;
import models.AnonymousUser;
import models.Customer;
import models.OneTimePassword;
import models.Transaction;
import models.User;
import models.UserSession;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;
import store.DataStoreEvent;

@org.springframework.stereotype.Service
public class D3ESubscription implements FlowableOnSubscribe<DataStoreEvent> {
  public ConnectableFlowable<DataStoreEvent> flowable;
  private FlowableEmitter<DataStoreEvent> emitter;

  @PostConstruct
  public void init() {
    this.flowable = Flowable.create(this, BackpressureStrategy.BUFFER).publish();
    this.flowable.connect();
    flowable.subscribe((a) -> {});
  }

  @Async
  @TransactionalEventListener
  public void handleContextStart(DataStoreEvent event) {
    this.emitter.onNext(event);
  }

  @Override
  public void subscribe(FlowableEmitter<DataStoreEvent> emitter) throws Throwable {
    this.emitter = emitter;
  }

  public Flowable<D3ESubscriptionEvent<AnonymousUser>> onAnonymousUserChangeEvent() {
    return this.flowable
        .filter((e) -> e.getEntity() instanceof AnonymousUser)
        .map(
            (e) -> {
              D3ESubscriptionEvent<AnonymousUser> event = new D3ESubscriptionEvent<>();
              event.model = ((AnonymousUser) e.getEntity());
              event.changeType = e.getType();
              return event;
            });
  }

  public Flowable<D3ESubscriptionEvent<Customer>> onCustomerChangeEvent() {
    return this.flowable
        .filter((e) -> e.getEntity() instanceof Customer)
        .map(
            (e) -> {
              D3ESubscriptionEvent<Customer> event = new D3ESubscriptionEvent<>();
              event.model = ((Customer) e.getEntity());
              event.changeType = e.getType();
              return event;
            });
  }

  public Flowable<D3ESubscriptionEvent<OneTimePassword>> onOneTimePasswordChangeEvent() {
    return this.flowable
        .filter((e) -> e.getEntity() instanceof OneTimePassword)
        .map(
            (e) -> {
              D3ESubscriptionEvent<OneTimePassword> event = new D3ESubscriptionEvent<>();
              event.model = ((OneTimePassword) e.getEntity());
              event.changeType = e.getType();
              return event;
            });
  }

  public Flowable<D3ESubscriptionEvent<Transaction>> onTransactionChangeEvent() {
    return this.flowable
        .filter((e) -> e.getEntity() instanceof Transaction)
        .map(
            (e) -> {
              D3ESubscriptionEvent<Transaction> event = new D3ESubscriptionEvent<>();
              event.model = ((Transaction) e.getEntity());
              event.changeType = e.getType();
              return event;
            });
  }

  public Flowable<D3ESubscriptionEvent<User>> onUserChangeEvent() {
    return this.flowable
        .filter((e) -> e.getEntity() instanceof User)
        .map(
            (e) -> {
              D3ESubscriptionEvent<User> event = new D3ESubscriptionEvent<>();
              event.model = ((User) e.getEntity());
              event.changeType = e.getType();
              return event;
            });
  }

  public Flowable<D3ESubscriptionEvent<UserSession>> onUserSessionChangeEvent() {
    return this.flowable
        .filter((e) -> e.getEntity() instanceof UserSession)
        .map(
            (e) -> {
              D3ESubscriptionEvent<UserSession> event = new D3ESubscriptionEvent<>();
              event.model = ((UserSession) e.getEntity());
              event.changeType = e.getType();
              return event;
            });
  }
}
