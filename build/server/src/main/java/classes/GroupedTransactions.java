package classes;

import java.util.List;

public class GroupedTransactions {
  public List<GTxn> items;
  public long id;

  public GroupedTransactions() {}

  public GroupedTransactions(List<GTxn> items) {
    this.items = items;
  }
}
