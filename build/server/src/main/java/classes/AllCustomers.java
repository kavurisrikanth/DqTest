package classes;

import java.util.List;
import models.Customer;

public class AllCustomers {
  public long id;
  public List<Customer> items;

  public AllCustomers() {}

  public AllCustomers(List<Customer> items) {
    this.items = items;
  }
}
