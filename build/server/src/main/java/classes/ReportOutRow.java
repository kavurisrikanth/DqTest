package classes;

import java.util.List;

public class ReportOutRow {
  public String key;
  public String parentKey;
  public List<ReportOutCell> cells;
  public String groupingKey;

  public ReportOutRow() {}

  public ReportOutRow(String key, String parentKey, List<ReportOutCell> cells, String groupingKey) {
    this.key = key;
    this.parentKey = parentKey;
    this.cells = cells;
    this.groupingKey = groupingKey;
  }
}
