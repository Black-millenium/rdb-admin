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
package workbench.gui.dbobjects.objecttree;

import workbench.db.ColumnIdentifier;
import workbench.db.ConnectionMgr;
import workbench.db.DbObject;
import workbench.db.WbConnection;
import workbench.gui.sql.EditorPanel;

/**
 * @author Thomas Kellerer
 */
public class EditorDropHandler {
  private EditorPanel editor;

  public EditorDropHandler(EditorPanel editor) {
    this.editor = editor;
  }

  public void handleDrop(ObjectTreeTransferable selection) {
    if (selection == null) return;
    ObjectTreeNode[] nodes = selection.getSelectedNodes();
    if (nodes == null || nodes.length == 0) return;

    String id = selection.getConnectionId();
    WbConnection conn = ConnectionMgr.getInstance().findConnection(id);
    StringBuilder text = new StringBuilder(nodes.length * 20);
    boolean first = true;
    for (ObjectTreeNode node : nodes) {
      if (first) first = false;
      else text.append(", ");
      text.append(getDisplayString(conn, node));
    }
    editor.setSelectedText(text.toString());
  }

  private String getDisplayString(WbConnection conn, ObjectTreeNode node) {
    if (node == null) return "";
    DbObject dbo = node.getDbObject();
    if (dbo == null) {
      if (TreeLoader.TYPE_COLUMN_LIST.equals(node.getType())) {
        return getColumnList(node);
      }
      return node.getName();
    }
    return dbo.getObjectExpression(conn);
  }

  private String getColumnList(ObjectTreeNode columns) {
    int count = columns.getChildCount();
    StringBuilder result = new StringBuilder(count * 10);
    int colCount = 0;
    for (int i = 0; i < count; i++) {
      ObjectTreeNode col = (ObjectTreeNode) columns.getChildAt(i);
      if (col != null && col.getDbObject() != null) {
        DbObject dbo = col.getDbObject();
        if (dbo instanceof ColumnIdentifier) {
          if (colCount > 0) result.append(", ");
          result.append(dbo.getObjectName());
          colCount++;
        }
      }
    }
    return result.toString();
  }
}
