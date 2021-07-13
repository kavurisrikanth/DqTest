package classes;

import java.util.List;

public class ReportOutput {
  public List<ReportOutOption> options;
  public List<ReportOutColumn> columns;
  public List<ReportOutColumn> subColumns;
  public List<ReportOutAttribute> attributes;
  public List<ReportOutRow> rows;

  public ReportOutput() {}

  public ReportOutput(
      List<ReportOutOption> options,
      List<ReportOutColumn> columns,
      List<ReportOutColumn> subColumns,
      List<ReportOutAttribute> attributes,
      List<ReportOutRow> rows) {
    this.options = options;
    this.columns = columns;
    this.subColumns = subColumns;
    this.attributes = attributes;
    this.rows = rows;
  }
}
