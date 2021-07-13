package classes;

import java.util.List;
import models.Transaction;

public class OrderedTransactions {
  public List<Transaction> items;

  public OrderedTransactions() {}

  public OrderedTransactions(List<Transaction> items) {
    this.items = items;
  }
}
