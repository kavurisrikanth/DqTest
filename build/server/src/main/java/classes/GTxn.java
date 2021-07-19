package classes;

import java.util.List;
import models.Customer;
import models.Transaction;

public class GTxn {
  public Customer customer;
  public List<Transaction> txns;

  public GTxn() {}

  public GTxn(Customer customer, List<Transaction> txns) {
    this.customer = customer;
    this.txns = txns;
  }
}
