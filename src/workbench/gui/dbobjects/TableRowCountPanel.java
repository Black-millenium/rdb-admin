/*
 * TableRowCountPanel.java
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
package workbench.gui.dbobjects;

import workbench.WbManager;
import workbench.db.*;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.ReloadAction;
import workbench.gui.actions.StopAction;
import workbench.gui.components.DataStoreTableModel;
import workbench.gui.components.RunningJobIndicator;
import workbench.gui.components.WbTable;
import workbench.gui.components.WbToolbar;
import workbench.interfaces.Interruptable;
import workbench.interfaces.Reloadable;
import workbench.interfaces.ToolWindow;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.sql.wbcommands.WbRowCount;
import workbench.storage.DataStore;
import workbench.storage.SortDefinition;
import workbench.util.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Thomas Kellerer
 */
public class TableRowCountPanel
    extends JPanel
    implements WindowListener, Reloadable, Interruptable, ToolWindow {
  private static int instanceCount;
  private final WbTable data;
  private final JLabel statusBar;
  private final List<TableIdentifier> tables;
  private final WbConnection sourceConnection;
  private final StopAction cancelAction;
  private final JScrollPane scrollPane;
  private Statement currentStatement;
  private volatile boolean cancel;
  private JFrame window;
  private WbConnection dbConnection;
  private boolean useSeparateConnection;

  public TableRowCountPanel(List<TableIdentifier> toCount, WbConnection connection) {
    super(new BorderLayout(0, 0));
    tables = toCount;
    instanceCount++;
    sourceConnection = connection;

    statusBar = new JLabel();
    data = new WbTable(false, false, false);
    data.setReadOnly(true);

    scrollPane = new JScrollPane(data);
    JPanel statusPanel = new JPanel(new BorderLayout(0, 0));

    Border etched = new EtchedBorder(EtchedBorder.LOWERED);
    Border current = scrollPane.getBorder();
    Border frame = new CompoundBorder(new EmptyBorder(3, 3, 0, 3), current);
    scrollPane.setBorder(frame);

    Border b = new CompoundBorder(new EmptyBorder(3, 2, 2, 3), etched);
    statusPanel.setBorder(b);
    statusPanel.add(statusBar);

    WbToolbar toolbar = new WbToolbar();
    toolbar.setBorder(new CompoundBorder(new EmptyBorder(3, 3, 3, 3), etched));
    ReloadAction reload = new ReloadAction(this);
    toolbar.add(reload);
    toolbar.addSeparator();
    cancelAction = new StopAction(this);
    cancelAction.setEnabled(false);
    toolbar.add(cancelAction);

    add(toolbar, BorderLayout.PAGE_START);
    add(scrollPane, BorderLayout.CENTER);
    add(statusPanel, BorderLayout.PAGE_END);
  }

  private void checkConnection() {
    if (dbConnection != null) return;

    if (sourceConnection.getProfile().getUseSeparateConnectionPerTab()) {
      try {
        showStatusMessage(ResourceMgr.getString("MsgConnecting"));
        dbConnection = ConnectionMgr.getInstance().getConnection(sourceConnection.getProfile(), "TableRowCount-" + NumberStringCache.getNumberString(instanceCount));
      } catch (Exception cne) {
        LogMgr.logError("TableRowCountPanel.checkConnection()", "Could not get connection", cne);
      } finally {
        showStatusMessage("");
      }
      useSeparateConnection = true;
    } else {
      dbConnection = sourceConnection;
      useSeparateConnection = false;
    }
  }

  @Override
  public void reload() {
    retrieveRowCounts();
  }

  @Override
  public boolean confirmCancel() {
    return true;
  }

  @Override
  public void cancelExecution() {
    showStatusMessage(ResourceMgr.getString("MsgCancelling"));
    cancel = true;
    if (currentStatement != null) {
      LogMgr.logDebug("TableRowCountPanel.cancel()", "Trying to cancel the current statement");
      try {
        currentStatement.cancel();
      } catch (SQLException sql) {
        LogMgr.logWarning("TableRowCountPanel.cancel()", "Could not cancel statement", sql);
      }
    }
  }

  private void connectAndRetrieve() {
    WbThread conn = new WbThread("RowCountConnect") {
      @Override
      public void run() {
        checkConnection();
        doRetrieveRowCounts();
      }
    };
    conn.start();

  }

  public void retrieveRowCounts() {
    if (CollectionUtil.isEmpty(tables)) return;

    if (dbConnection == null) {
      connectAndRetrieve();
      return;
    }

    WbThread retrieveThread = new WbThread("RowCounter") {
      @Override
      public void run() {
        doRetrieveRowCounts();
      }
    };
    retrieveThread.start();
  }

  private void doRetrieveRowCounts() {
    if (!WbSwingUtilities.isConnectionIdle(this, dbConnection)) {
      return;
    }

    cancelAction.setEnabled(true);
    cancel = false;

    DataStore ds = WbRowCount.buildResultDataStore(dbConnection);
    DataStoreTableModel model = new DataStoreTableModel(ds);
    model.setAllowEditing(false);
    setModel(model);
    ResultSet rs = null;

    try {
      dbConnection.setBusy(true);
      TableSelectBuilder builder = new TableSelectBuilder(dbConnection, TableSelectBuilder.TABLEDATA_TEMPLATE_NAME);
      currentStatement = dbConnection.createStatementForQuery();

      WbSwingUtilities.showWaitCursor(scrollPane);

      this.window.setTitle(RunningJobIndicator.TITLE_PREFIX + ResourceMgr.getString("TxtWindowTitleRowCount"));
      boolean useSavepoint = dbConnection.getDbSettings().useSavePointForDML();

      int tblCount = tables.size();
      for (int tableNum = 0; tableNum < tblCount; tableNum++) {
        if (cancel) break;

        TableIdentifier table = tables.get(tableNum);
        showTable(table, tableNum + 1, tblCount);
        String sql = builder.getSelectForCount(table);

        rs = JdbcUtils.runStatement(dbConnection, currentStatement, sql, useSeparateConnection, useSavepoint);

        if (cancel) break;

        long rowCount = 0;
        if (rs == null) {
          rowCount = -1;
        } else if (rs.next()) {
          rowCount = rs.getLong(1);
        }
        SqlUtil.closeResult(rs);
        addRowCount(table, rowCount);
      }
    } catch (SQLException sql) {
      LogMgr.logError("TableRowCountPanel.retrieveRowCounts()", "Error retrieving table count", sql);
    } finally {
      SqlUtil.closeAll(rs, currentStatement);
      currentStatement = null;
      dbConnection.setBusy(false);
      showStatusMessage("");
      WbSwingUtilities.showDefaultCursor(scrollPane);
      window.setTitle(ResourceMgr.getString("TxtWindowTitleRowCount"));
      data.checkCopyActions();
      cancelAction.setEnabled(false);
    }

    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        SortDefinition sortDef = WbRowCount.getDefaultRowCountSort(data.getDataStore(), dbConnection);
        data.getDataStoreTableModel().setSortDefinition(sortDef);
        data.requestFocusInWindow();
      }
    });
  }

  private void showTable(final TableIdentifier table, int current, int total) {
    String msg = ResourceMgr.getFormattedString("MsgCalculatingRowCount", table.getTableExpression(), current, total);
    showStatusMessage(msg);
  }

  private void showStatusMessage(String message) {
    final String msg;
    if (StringUtil.isEmptyString(message)) {
      msg = ResourceMgr.getFormattedString("TxtTableListObjects", data.getRowCount());
    } else {
      msg = message;
    }
    WbSwingUtilities.invoke(new Runnable() {
      @Override
      public void run() {
        statusBar.setText(" " + msg);
      }
    });
  }

  private void addRowCount(final TableIdentifier table, final long count) {
    WbSwingUtilities.invoke(new Runnable() {
      @Override
      public void run() {
        DataStoreTableModel model = data.getDataStoreTableModel();
        int row = model.addRow();
        model.setValueAt(Long.valueOf(count), row, 0);
        model.setValueAt(table.getTableName(), row, 1);
        model.setValueAt(table.getObjectType(), row, 2);
        model.setValueAt(table.getCatalog(), row, 3);
        model.setValueAt(table.getSchema(), row, 4);
        data.adjustRowsAndColumns();
      }
    });
  }

  private void setModel(final DataStoreTableModel model) {
    WbSwingUtilities.invoke(new Runnable() {
      @Override
      public void run() {
        data.setModel(model, true);
      }
    });
  }

  public void showWindow(Window aParent) {
    if (this.window == null) {
      this.window = new JFrame(ResourceMgr.getString("TxtWindowTitleRowCount"));
      this.window.getContentPane().setLayout(new BorderLayout());
      this.window.getContentPane().add(this, BorderLayout.CENTER);

      ResourceMgr.setWindowIcons(window, "rowcounts");

      if (!Settings.getInstance().restoreWindowSize(this.window, getClass().getName())) {
        this.window.setSize(500, 400);
      }

      if (!Settings.getInstance().restoreWindowPosition(this.window, getClass().getName())) {
        WbSwingUtilities.center(this.window, aParent);
      }
      this.window.addWindowListener(this);
      WbManager.getInstance().registerToolWindow(this);
    }
    this.window.setVisible(true);
    this.retrieveRowCounts();
  }

  protected void saveSettings() {
    Settings.getInstance().storeWindowPosition(this.window, getClass().getName());
    Settings.getInstance().storeWindowSize(this.window, getClass().getName());
  }

  private void doClose() {
    cancelExecution();
    saveSettings();
    this.window.setVisible(false);
    this.window.dispose();
    this.window = null;
  }

  @Override
  public void windowOpened(WindowEvent e) {
  }

  @Override
  public void windowClosing(WindowEvent e) {
    WbManager.getInstance().unregisterToolWindow(this);
    doClose();
  }


  @Override
  public void windowClosed(WindowEvent e) {
    disconnect();
  }

  @Override
  public void windowIconified(WindowEvent e) {

  }

  @Override
  public void windowDeiconified(WindowEvent e) {

  }

  @Override
  public void windowActivated(WindowEvent e) {

  }

  @Override
  public void windowDeactivated(WindowEvent e) {
  }

  @Override
  public void closeWindow() {
    doClose();
  }

  @Override
  public void disconnect() {
    if (useSeparateConnection && dbConnection != null) {
      dbConnection.disconnect();
      dbConnection = null;
    }
  }

  @Override
  public void activate() {
    if (window != null) {
      window.requestFocus();
    }
  }

  @Override
  public WbConnection getConnection() {
    return dbConnection;
  }

  @Override
  public JFrame getWindow() {
    return window;
  }

}
