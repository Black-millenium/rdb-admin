/*
 * ColumnRemover.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.storage;

import workbench.db.ColumnIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to remove columns from a DataStore.
 *
 * @author Thomas Kellerer
 */
public class ColumnRemover {
  private DataStore original;

  public ColumnRemover(DataStore ds) {
    original = ds;
  }

  /**
   * Remove the named columns from the DataStore
   *
   * @param colNames
   * @return a new DataStore with the named columns removed
   * @see #removeColumnsByIndex(java.util.List)
   */
  public DataStore removeColumnsByName(String... colNames) {
    List<Integer> cols = new ArrayList<Integer>(colNames.length);
    for (String name : colNames) {
      int index = original.getColumnIndex(name);
      if (index > -1) {
        cols.add(Integer.valueOf(index));
      }
    }
    return removeColumnsByIndex(cols);
  }

  /**
   * Remove the named columns from the DataStore.
   * <p/>
   * The new DataStore will have any update flags reset (so isModified()
   * on the new DataStore will return false).
   * <p/>
   * The new DataStore will contain all rows from the old DataStore that have
   * not been filtered or deleted
   *
   * @param toRemove a list of column indexes to remove
   * @return a new DataStore with the columns removed
   */
  public DataStore removeColumnsByIndex(List<Integer> toRemove) {
    ResultInfo info = original.getResultInfo();
    List<ColumnIdentifier> cols = new ArrayList<ColumnIdentifier>(info.getColumnCount());

    for (int i = 0; i < info.getColumnCount(); i++) {
      Integer index = Integer.valueOf(i);
      if (!toRemove.contains(index)) {
        cols.add(info.getColumn(i));
      }
    }

    ColumnIdentifier[] newCols = new ColumnIdentifier[cols.size()];
    cols.toArray(newCols);

    ResultInfo newInfo = new ResultInfo(newCols);
    DataStore newDs = new DataStore(newInfo);

    for (int row = 0; row < original.getRowCount(); row++) {
      int newRow = newDs.addRow();

      for (int i = 0; i < newInfo.getColumnCount(); i++) {
        ColumnIdentifier col = newInfo.getColumn(i);
        String colname = col.getColumnName();
        Object value = original.getValue(row, colname);
        newDs.setValue(newRow, i, value);
      }
      newDs.getRow(newRow).setUserObject(original.getRow(row).getUserObject());
    }
    newDs.resetStatus();
    return newDs;
  }
}
