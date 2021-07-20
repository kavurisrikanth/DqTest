package classes;

import java.util.List;
import models.Transaction;

public class OrderedFilteredTransactions {
  public long id;
  public List<Transaction> items;

  public OrderedFilteredTransactions() {}

  public OrderedFilteredTransactions(List<Transaction> items) {
    this.items = items;
  }
}
