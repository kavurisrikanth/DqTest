package d3e.core;

import classes.AllTransactions;
import classes.FilteredTransactions;
import classes.FilteredTransactionsRequest;
import classes.GroupedTransactions;
import classes.GroupedTransactionsRequest;
import classes.LoginResult;
import classes.OrderedTransactions;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lists.AllTransactionsImpl;
import lists.FilteredTransactionsImpl;
import lists.GroupedTransactionsImpl;
import lists.OrderedTransactionsImpl;
import models.AnonymousUser;
import models.Customer;
import models.OneTimePassword;
import models.Transaction;
import models.User;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.AnonymousUserRepository;
import repository.jpa.AvatarRepository;
import repository.jpa.CustomerRepository;
import repository.jpa.OneTimePasswordRepository;
import repository.jpa.ReportConfigOptionRepository;
import repository.jpa.ReportConfigRepository;
import repository.jpa.TransactionRepository;
import repository.jpa.UserRepository;
import repository.jpa.UserSessionRepository;
import security.AppSessionProvider;
import security.JwtTokenUtil;

@Service
public class QueryProvider {
  public static QueryProvider instance;
  @Autowired private JwtTokenUtil jwtTokenUtil;
  @Autowired private AnonymousUserRepository anonymousUserRepository;
  @Autowired private AvatarRepository avatarRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private OneTimePasswordRepository oneTimePasswordRepository;
  @Autowired private ReportConfigRepository reportConfigRepository;
  @Autowired private ReportConfigOptionRepository reportConfigOptionRepository;
  @Autowired private TransactionRepository transactionRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private UserSessionRepository userSessionRepository;
  @Autowired private AllTransactionsImpl allTransactionsImpl;
  @Autowired private FilteredTransactionsImpl filteredTransactionsImpl;
  @Autowired private GroupedTransactionsImpl groupedTransactionsImpl;
  @Autowired private OrderedTransactionsImpl orderedTransactionsImpl;
  @Autowired private ObjectFactory<AppSessionProvider> provider;

  @PostConstruct
  public void init() {
    instance = this;
  }

  public static QueryProvider get() {
    return instance;
  }

  public AnonymousUser getAnonymousUserById(long id) {
    Optional<AnonymousUser> findById = anonymousUserRepository.findById(id);
    return findById.orElse(null);
  }

  public Customer getCustomerById(long id) {
    Optional<Customer> findById = customerRepository.findById(id);
    return findById.orElse(null);
  }

  public OneTimePassword getOneTimePasswordById(long id) {
    Optional<OneTimePassword> findById = oneTimePasswordRepository.findById(id);
    return findById.orElse(null);
  }

  public boolean checkTokenUniqueInOneTimePassword(long oneTimePasswordId, String token) {
    return oneTimePasswordRepository.checkTokenUnique(oneTimePasswordId, token);
  }

  public Transaction getTransactionById(long id) {
    Optional<Transaction> findById = transactionRepository.findById(id);
    return findById.orElse(null);
  }

  public AllTransactions getAllTransactions() {
    return allTransactionsImpl.get();
  }

  public FilteredTransactions getFilteredTransactions(FilteredTransactionsRequest inputs) {
    return filteredTransactionsImpl.get(inputs);
  }

  public GroupedTransactions getGroupedTransactions(GroupedTransactionsRequest inputs) {
    return groupedTransactionsImpl.get(inputs);
  }

  public OrderedTransactions getOrderedTransactions() {
    return orderedTransactionsImpl.get();
  }

  public LoginResult loginWithOTP(String token, String code, String deviceToken) {
    OneTimePassword otp = oneTimePasswordRepository.getByToken(token);
    User user = otp.getUser();
    LoginResult loginResult = new LoginResult();
    if (deviceToken != null) {
      user.setDeviceToken(deviceToken);
    }
    loginResult.success = true;
    loginResult.userObject = otp.getUser();
    loginResult.token = token;
    return loginResult;
  }
}