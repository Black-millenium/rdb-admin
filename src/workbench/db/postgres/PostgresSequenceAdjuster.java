/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015 Thomas Kellerer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.db.postgres;

import workbench.db.SequenceAdjuster;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.util.SqlUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to sync the sequences related to the columns of a table with the current values.
 * <p/>
 * This is intended to be used after doing bulk inserts into the database.
 *
 * @author Thomas Kellerer
 */
public class PostgresSequenceAdjuster
    implements SequenceAdjuster {
  public PostgresSequenceAdjuster() {
  }

  @Override
  public int adjustTableSequences(WbConnection connection, TableIdentifier table, boolean includeCommit)
      throws SQLException {
    Map<String, String> columns = getColumnSequences(connection, table);

    for (Map.Entry<String, String> entry : columns.entrySet()) {
      syncSingleSequence(connection, table, entry.getKey(), entry.getValue());
    }

    if (includeCommit && !connection.getAutoCommit()) {
      connection.commit();
    }
    return columns.size();
  }

  private void syncSingleSequence(WbConnection dbConnection, TableIdentifier table, String column, String sequence)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    Savepoint sp = null;
    String sql =
        "select setval('" + sequence + "', (select max(" + column + ") from " + table.getTableExpression(dbConnection) + "))";

    LogMgr.logDebug("PostgresSequenceSync.syncSingleSequence()", "Syncing sequence using: " + sql);

    try {
      sp = dbConnection.setSavepoint();
      stmt = dbConnection.createStatement();
      rs = stmt.executeQuery(sql);
      if (rs.next()) {
        long newValue = rs.getLong(1);
        LogMgr.logDebug("PostgresSequenceSync.syncSingleSequence()", "New value for sequence " + sequence + " is: " + newValue);
      }
      dbConnection.releaseSavepoint(sp);
    } catch (SQLException ex) {
      dbConnection.rollback(sp);
      LogMgr.logError("PostgresSequenceSync.getColumnSequences()", "Could not read sequences", ex);
      throw ex;
    } finally {
      SqlUtil.closeAll(rs, stmt);
    }

  }

  private Map<String, String> getColumnSequences(WbConnection dbConnection, TableIdentifier table) {
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    String sql =
        "select * \n" +
            "from ( \n" +
            "  select column_name,  \n" +
            "         pg_get_serial_sequence(?, column_name) as sequence_name \n" +
            "  from information_schema.columns \n" +
            "  where table_name = ? \n" +
            "  and table_schema = ? \n" +
            ") t \n" +
            "where sequence_name is not null";

    Map<String, String> result = new HashMap<String, String>();
    try {
      pstmt = dbConnection.getSqlConnection().prepareStatement(sql);
      pstmt.setString(1, table.getRawTableName());
      pstmt.setString(2, table.getRawTableName());
      pstmt.setString(3, table.getRawSchema());

      rs = pstmt.executeQuery();
      while (rs.next()) {
        String column = rs.getString(1);
        String seq = rs.getString(2);
        result.put(column, seq);
      }
    } catch (SQLException ex) {
      LogMgr.logError("PostgresSequenceSync.getColumnSequences()", "Could not read sequences", ex);
    } finally {
      SqlUtil.closeAll(rs, pstmt);
    }
    return result;
  }

}
