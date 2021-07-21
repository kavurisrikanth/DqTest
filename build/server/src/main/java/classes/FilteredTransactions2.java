package classes;

import java.util.List;
import models.Transaction;

public class FilteredTransactions2 {
  public long id;
  public List<Transaction> items;

  public FilteredTransactions2() {}

  public FilteredTransactions2(List<Transaction> items) {
    this.items = items;
  }
}
