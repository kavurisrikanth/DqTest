package classes;

import java.util.List;
import models.Transaction;

public class FemaleTransactionsOrderByAmountAndAge {
  public long id;
  public List<Transaction> items;

  public FemaleTransactionsOrderByAmountAndAge() {}

  public FemaleTransactionsOrderByAmountAndAge(List<Transaction> items) {
    this.items = items;
  }
}
