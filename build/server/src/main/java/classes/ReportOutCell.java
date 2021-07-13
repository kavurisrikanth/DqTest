package classes;

import java.util.List;

public class ReportOutCell {
  public String key;
  public String type;
  public String value;
  public List<ReportOutAttribute> attributes;

  public ReportOutCell() {}

  public ReportOutCell(String key, String type, String value, List<ReportOutAttribute> attributes) {
    this.key = key;
    this.type = type;
    this.value = value;
    this.attributes = attributes;
  }
}
