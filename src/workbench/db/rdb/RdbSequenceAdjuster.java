package workbench.db.rdb;

import workbench.db.ColumnIdentifier;
import workbench.db.SequenceAdjuster;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.util.SqlUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RdbSequenceAdjuster
    implements SequenceAdjuster {
  public RdbSequenceAdjuster() {
  }

  @Override
  public int adjustTableSequences(WbConnection connection, TableIdentifier table, boolean includeCommit)
      throws SQLException {
    List<String> columns = getIdentityColumns(connection, table);

    for (String column : columns) {
      syncSingleSequence(connection, table, column);
    }

    if (includeCommit && !connection.getAutoCommit()) {
      connection.commit();
    }
    return columns.size();
  }

  private void syncSingleSequence(WbConnection dbConnection, TableIdentifier table, String column)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = dbConnection.createStatement();

      long maxValue = -1;
      rs = stmt.executeQuery("select max(" + column + ") from " + table.getTableExpression(dbConnection));

      if (rs.next()) {
        maxValue = rs.getLong(1) + 1;
        SqlUtil.closeResult(rs);
      }

      if (maxValue > 0) {
        String ddl = "alter table " + table.getTableExpression(dbConnection) + " alter column " + column + " restart with " + Long.toString(maxValue);
        LogMgr.logDebug("RdbSequenceAdjuster.syncSingleSequence()", "Syncing sequence using: " + ddl);
        stmt.execute(ddl);
      }
    } catch (SQLException ex) {
      LogMgr.logError("RdbSequenceAdjuster.getColumnSequences()", "Could not read sequences", ex);
      throw ex;
    } finally {
      SqlUtil.closeAll(rs, stmt);
    }
  }

  private List<String> getIdentityColumns(WbConnection dbConnection, TableIdentifier table) {
    List<String> result = new ArrayList<String>(1);
    try {
      List<ColumnIdentifier> columns = dbConnection.getMetadata().getTableColumns(table, false);
      for (ColumnIdentifier col : columns) {
        if (col.isAutoincrement()) {
          result.add(col.getColumnName());
        }
      }
    } catch (SQLException ex) {
      LogMgr.logError("RdbSequenceAdjuster.getIdentityColumns()", "Could not read sequence columns", ex);
    }
    return result;
  }

}
