package rest.ws;

import d3e.core.D3ELogger;
import gqltosql.schema.IModelSchema;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import repository.jpa.AnonymousUserRepository;
import repository.jpa.CustomerRepository;
import repository.jpa.OneTimePasswordRepository;
import repository.jpa.TransactionRepository;
import repository.jpa.UserRepository;
import repository.jpa.UserSessionRepository;
import security.AppSessionProvider;
import store.DatabaseObject;
import store.EntityHelperService;
import store.EntityMutator;

@org.springframework.stereotype.Service
public class RocketMutation extends AbstractRocketQuery {
  @Autowired private EntityMutator mutator;
  @Autowired private ObjectFactory<EntityHelperService> helperService;
  @Autowired private IModelSchema schema;
  @Autowired private AnonymousUserRepository anonymousUserRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private OneTimePasswordRepository oneTimePasswordRepository;
  @Autowired private TransactionRepository transactionRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private UserSessionRepository userSessionRepository;
  @Autowired private ObjectFactory<AppSessionProvider> provider;

  public void save(String type, DatabaseObject obj) throws Exception {
    boolean create = obj.isNew();
    switch (type) {
    }
    D3ELogger.info("Mutation Not found");
  }

  public void delete(String type, long id) throws Exception {
    switch (type) {
    }
    D3ELogger.info("Mutation Not found");
  }
}
