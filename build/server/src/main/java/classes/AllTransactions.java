package classes;

import java.util.List;
import models.Transaction;

public class AllTransactions {
  public long id;
  public List<Transaction> items;

  public AllTransactions() {}

  public AllTransactions(List<Transaction> items) {
    this.items = items;
  }
}
