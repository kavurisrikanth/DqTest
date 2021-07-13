package classes;

import java.util.List;

public class ReportOutColumn {
  public String type;
  public String value;
  public List<ReportOutAttribute> attributes;

  public ReportOutColumn() {}

  public ReportOutColumn(String type, String value, List<ReportOutAttribute> attributes) {
    this.type = type;
    this.value = value;
    this.attributes = attributes;
  }
}
