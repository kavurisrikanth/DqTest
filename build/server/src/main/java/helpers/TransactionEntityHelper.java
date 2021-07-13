package helpers;

import models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.CustomerRepository;
import repository.jpa.TransactionRepository;
import rest.GraphQLInputContext;
import store.EntityHelper;
import store.EntityMutator;
import store.EntityValidationContext;

@Service("Transaction")
public class TransactionEntityHelper<T extends Transaction> implements EntityHelper<T> {
  @Autowired protected EntityMutator mutator;
  @Autowired private TransactionRepository transactionRepository;
  @Autowired private CustomerRepository customerRepository;

  public void setMutator(EntityMutator obj) {
    mutator = obj;
  }

  public Transaction newInstance() {
    return new Transaction();
  }

  @Override
  public void fromInput(T entity, GraphQLInputContext ctx) {
    if (ctx.has("customer")) {
      entity.setCustomer(ctx.readRef("customer", "Customer"));
    }
    if (ctx.has("amount")) {
      entity.setAmount(ctx.readDouble("amount"));
    }
    entity.updateMasters((o) -> {});
  }

  public void referenceFromValidations(T entity, EntityValidationContext validationContext) {}

  public void validateInternal(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {}

  public void validateOnCreate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, true, false);
  }

  public void validateOnUpdate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, false, true);
  }

  @Override
  public T clone(T entity) {
    return null;
  }

  @Override
  public T getById(long id) {
    return id == 0l ? null : ((T) transactionRepository.findById(id).orElse(null));
  }

  @Override
  public void setDefaults(T entity) {}

  @Override
  public void compute(T entity) {}

  public Boolean onDelete(T entity, boolean internal, EntityValidationContext deletionContext) {
    return true;
  }

  @Override
  public Boolean onCreate(T entity, boolean internal) {
    return true;
  }

  @Override
  public Boolean onUpdate(T entity, boolean internal) {
    return true;
  }

  public T getOld(long id) {
    return ((T) getById(id).clone());
  }
}
