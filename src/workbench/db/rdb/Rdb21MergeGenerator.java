package workbench.db.rdb;

import workbench.db.ColumnIdentifier;
import workbench.db.TableIdentifier;
import workbench.storage.*;

public class Rdb21MergeGenerator
    implements MergeGenerator {
  private SqlLiteralFormatter formatter;

  public Rdb21MergeGenerator() {
    this.formatter = new SqlLiteralFormatter(SqlLiteralFormatter.ANSI_DATE_LITERAL_TYPE);
  }

  @Override
  public String generateMergeStart(RowDataContainer data) {
    StringBuilder result = new StringBuilder(100);
    generateStart(result, data, false);
    return result.toString();
  }

  @Override
  public String addRow(ResultInfo info, RowData row, long rowIndex) {
    StringBuilder sql = new StringBuilder(100);
    if (rowIndex > 0) sql.append("\n  UNION ALL\n");
    appendValues(sql, info, row, rowIndex == 0);
    return sql.toString();
  }

  @Override
  public String generateMergeEnd(RowDataContainer data) {
    StringBuilder sql = new StringBuilder(data.getRowCount());
    appendJoin(sql, data);
    appendUpdate(sql, data);
    appendInsert(sql, data);
    return sql.toString();
  }

  @Override
  public String generateMerge(RowDataContainer data) {
    StringBuilder sql = new StringBuilder(data.getRowCount());

    generateStart(sql, data, true);
    appendJoin(sql, data);
    appendUpdate(sql, data);
    appendInsert(sql, data);
    return sql.toString();
  }

  private void generateStart(StringBuilder sql, RowDataContainer data, boolean withData) {
    TableIdentifier tbl = data.getUpdateTable();
    sql.append("MERGE INTO ");
    sql.append(tbl.getTableExpression(data.getOriginalConnection()));
    sql.append(" ut\nUSING (\n");
    if (withData) {
      ResultInfo info = data.getResultInfo();
      for (int row = 0; row < data.getRowCount(); row++) {
        if (row > 0) sql.append("\n  UNION ALL\n");
        appendValues(sql, info, data.getRow(row), row == 0);
      }
    }
  }

  private void appendJoin(StringBuilder sql, RowDataContainer data) {
    ResultInfo info = data.getResultInfo();
    sql.append("\n) md ON (");
    int pkCount = 0;
    for (int col = 0; col < info.getColumnCount(); col++) {
      ColumnIdentifier colid = info.getColumn(col);
      if (!colid.isPkColumn()) continue;
      if (pkCount > 0) sql.append(" AND ");
      sql.append("ut.");
      sql.append(info.getColumnName(col));
      sql.append(" = md.");
      sql.append(info.getColumnName(col));
      pkCount++;
    }
    sql.append(")");
  }

  private void appendValues(StringBuilder sql, ResultInfo info, RowData rd, boolean useAlias) {
    sql.append("  SELECT ");

    for (int col = 0; col < info.getColumnCount(); col++) {
      if (col > 0) sql.append(", ");
      ColumnData cd = new ColumnData(rd.getValue(col), info.getColumn(col));
      sql.append(formatter.getDefaultLiteral(cd));
      if (useAlias) {
        sql.append(" AS ");
        sql.append(info.getColumnName(col));
      }
    }
    sql.append(" FROM rdb$database");
  }

  private void appendUpdate(StringBuilder sql, RowDataContainer data) {
    sql.append("\nWHEN MATCHED THEN UPDATE");
    ResultInfo info = data.getResultInfo();

    int colCount = 0;
    for (int col = 0; col < info.getColumnCount(); col++) {
      ColumnIdentifier id = info.getColumn(col);
      if (id.isPkColumn()) continue;
      if (colCount == 0) sql.append("\n     SET ");
      if (colCount > 0) sql.append(",\n         ");
      sql.append("ut.");
      sql.append(info.getColumnName(col));
      sql.append(" = md.");
      sql.append(info.getColumnName(col));
      colCount++;
    }
  }

  private void appendInsert(StringBuilder sql, RowDataContainer data) {
    sql.append("\nWHEN NOT MATCHED THEN\n  INSERT (");
    ResultInfo info = data.getResultInfo();
    StringBuilder columns = new StringBuilder(info.getColumnCount() * 10);
    for (int col = 0; col < info.getColumnCount(); col++) {
      if (col > 0) {
        sql.append(", ");
        columns.append(", ");
      }
      sql.append(info.getColumnName(col));
      columns.append("md.");
      columns.append(info.getColumnName(col));
    }
    sql.append(")\n");
    sql.append("  VALUES (");
    sql.append(columns);
    sql.append(");");
  }

}

