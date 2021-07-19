package helpers;

import classes.Gender;
import models.Customer;
import models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.CustomerRepository;
import repository.jpa.TransactionRepository;
import rest.GraphQLInputContext;
import store.EntityHelper;
import store.EntityMutator;
import store.EntityValidationContext;

@Service("Customer")
public class CustomerEntityHelper<T extends Customer> implements EntityHelper<T> {
  @Autowired protected EntityMutator mutator;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private TransactionRepository transactionRepository;

  public void setMutator(EntityMutator obj) {
    mutator = obj;
  }

  public Customer newInstance() {
    return new Customer();
  }

  @Override
  public void fromInput(T entity, GraphQLInputContext ctx) {
    if (ctx.has("name")) {
      entity.setName(ctx.readString("name"));
    }
    if (ctx.has("gender")) {
      entity.setGender(ctx.readEnum("gender", Gender.class));
    }
    if (ctx.has("age")) {
      entity.setAge(ctx.readInteger("age"));
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
    return id == 0l ? null : ((T) customerRepository.findById(id).orElse(null));
  }

  @Override
  public void setDefaults(T entity) {}

  @Override
  public void compute(T entity) {}

  private void deleteCustomerInTransaction(T entity, EntityValidationContext deletionContext) {
    if (EntityHelper.haveUnDeleted(this.transactionRepository.getByCustomer(entity))) {
      deletionContext.addEntityError(
          "This Customer cannot be deleted as it is being referred to by Transaction.");
    }
  }

  public Boolean onDelete(T entity, boolean internal, EntityValidationContext deletionContext) {
    return true;
  }

  public void validateOnDelete(T entity, EntityValidationContext deletionContext) {
    this.deleteCustomerInTransaction(entity, deletionContext);
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
