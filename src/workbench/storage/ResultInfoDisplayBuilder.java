/*
 * ResultInfoDisplayBuilder.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015, Thomas Kellerer.
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
package workbench.storage;

import workbench.db.ColumnIdentifier;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Kellerer
 */
public class ResultInfoDisplayBuilder {

  public static DataStore getDataStore(ResultInfo info, boolean showComments, boolean showTableName) {
    List<String> columns = new ArrayList<String>(12);
    columns.add("INDEX");
    columns.add("COLUMN_NAME");
    columns.add("ALIAS");
    columns.add("DATA_TYPE");
    columns.add("JDBC Type");
    if (showComments) {
      columns.add("REMARKS");
      columns.add("BASE TABLE");
    }
    columns.add("CLASS_NAME");
    columns.add("AUTO_GENERATED");
    columns.add("IDENTITY_COLUMN");
    columns.add("READONLY");
    columns.add("UPDATEABLE");
    if (showTableName) {
      columns.add("TABLE_NAME");
    }

    String[] cols = columns.toArray(new String[0]);
    int[] types = new int[cols.length];

    for (int i = 0; i < cols.length; i++) {
      if (cols[i].equals("JDBC TYPE")) {
        types[i] = Types.INTEGER;
      } else {
        types[i] = Types.VARCHAR;
      }
    }

    DataStore infoDs = new DataStore(cols, types);
    //for (ColumnIdentifier col : info.getColumns())
    for (int columnPosition = 0; columnPosition < info.getColumnCount(); columnPosition++) {
      int row = infoDs.addRow();
      int colIndex = 0;
      ColumnIdentifier col = info.getColumns()[columnPosition];
      int colPos = col.getPosition() == 0 ? columnPosition + 1 : col.getPosition();
      infoDs.setValue(row, colIndex++, colPos);
      infoDs.setValue(row, colIndex++, col.getColumnName());
      infoDs.setValue(row, colIndex++, col.getColumnAlias());
      infoDs.setValue(row, colIndex++, col.getDbmsType());
      infoDs.setValue(row, colIndex++, col.getDataType());
      if (showComments) {
        infoDs.setValue(row, colIndex++, col.getComment());
        infoDs.setValue(row, colIndex++, col.getSourceTableName());
      }
      infoDs.setValue(row, colIndex++, col.getColumnClassName());
      infoDs.setValue(row, colIndex++, col.isAutoincrement());
      infoDs.setValue(row, colIndex++, col.isIdentityColumn());
      infoDs.setValue(row, colIndex++, col.isReadonly());
      infoDs.setValue(row, colIndex++, col.isUpdateable());
      if (showTableName) {
        infoDs.setValue(row, colIndex, col.getSourceTableName());
      }
    }
    return infoDs;
  }

}
