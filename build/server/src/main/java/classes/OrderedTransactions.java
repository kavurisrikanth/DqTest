package classes;

import java.util.List;
import models.Transaction;

public class OrderedTransactions {
  public List<Transaction> items;
  public long id;

  public OrderedTransactions() {}

  public OrderedTransactions(List<Transaction> items) {
    this.items = items;
  }
}
