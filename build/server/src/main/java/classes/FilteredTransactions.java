package classes;

import java.util.List;
import models.Transaction;

public class FilteredTransactions {
  public List<Transaction> items;
  public long id;

  public FilteredTransactions() {}

  public FilteredTransactions(List<Transaction> items) {
    this.items = items;
  }
}
