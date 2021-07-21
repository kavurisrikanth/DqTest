package classes;

import java.util.List;
import models.Transaction;

public class StartFilteredTransactions {
  public long id;
  public List<Transaction> items;

  public StartFilteredTransactions() {}

  public StartFilteredTransactions(List<Transaction> items) {
    this.items = items;
  }
}
