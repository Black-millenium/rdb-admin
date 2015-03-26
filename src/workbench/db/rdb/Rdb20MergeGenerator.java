package workbench.db.rdb;

import workbench.storage.*;

public class Rdb20MergeGenerator
    implements MergeGenerator {
  private SqlLiteralFormatter formatter;
  private StatementFactory stmtFactory;

  public Rdb20MergeGenerator() {
    formatter = new SqlLiteralFormatter("rdb");
  }

  @Override
  public String generateMerge(RowDataContainer data) {
    StringBuilder result = new StringBuilder(data.getRowCount() * 100);
    ResultInfo info = data.getResultInfo();
    StatementFactory factory = new StatementFactory(info, data.getOriginalConnection());
    for (int row = 0; row < data.getRowCount(); row++) {
      result.append(generateUpsert(factory, info, data.getRow(row)));
      result.append('\n');
    }
    return result.toString();
  }

  @Override
  public String generateMergeStart(RowDataContainer data) {
    stmtFactory = new StatementFactory(data.getResultInfo(), data.getOriginalConnection());
    return "";
  }

  @Override
  public String addRow(ResultInfo info, RowData row, long rowIndex) {
    if (stmtFactory == null) {
      stmtFactory = new StatementFactory(info, null);
    }
    return generateUpsert(stmtFactory, info, row);
  }

  @Override
  public String generateMergeEnd(RowDataContainer data) {
    stmtFactory = null;
    return "";
  }

  private String generateUpsert(StatementFactory factory, ResultInfo info, RowData row) {
    DmlStatement dml = factory.createInsertStatement(row, true, "\n");
    CharSequence sql = dml.getExecutableStatement(formatter);
    StringBuilder result = new StringBuilder(sql.length() + 50);
    result.append("UPDATE OR ");
    result.append(sql);
    result.append("\nMATCHING (");
    int pkCount = 0;
    for (int col = 0; col < info.getColumnCount(); col++) {
      if (info.getColumn(col).isPkColumn()) {
        if (pkCount > 0) result.append(',');
        result.append(info.getColumn(0).getColumnName());
      }
    }
    result.append(");");
    return result.toString();
  }

}
