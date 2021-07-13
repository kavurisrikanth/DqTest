package helpers;

import models.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.UserSessionRepository;
import rest.GraphQLInputContext;
import store.EntityHelper;
import store.EntityMutator;
import store.EntityValidationContext;

@Service("UserSession")
public class UserSessionEntityHelper<T extends UserSession> implements EntityHelper<T> {
  @Autowired protected EntityMutator mutator;
  @Autowired private UserSessionRepository userSessionRepository;

  public void setMutator(EntityMutator obj) {
    mutator = obj;
  }

  @Override
  public void fromInput(T entity, GraphQLInputContext ctx) {
    if (ctx.has("userSessionId")) {
      entity.setUserSessionId(ctx.readString("userSessionId"));
    }
    entity.updateMasters((o) -> {});
  }

  public void referenceFromValidations(T entity, EntityValidationContext validationContext) {}

  public void validateFieldUserSessionId(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    String it = entity.getUserSessionId();
    if (it == null) {
      validationContext.addFieldError("userSessionId", "userSessionId is required.");
      return;
    }
  }

  public void validateInternal(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    validateFieldUserSessionId(entity, validationContext, onCreate, onUpdate);
  }

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
    return id == 0l ? null : ((T) userSessionRepository.findById(id).orElse(null));
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
