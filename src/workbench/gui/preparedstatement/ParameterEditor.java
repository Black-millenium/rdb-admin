/*
 * ParameterEditor.java
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
package workbench.gui.preparedstatement;

import workbench.WbManager;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.ValidatingDialog;
import workbench.gui.components.WbTable;
import workbench.gui.components.WbTextCellEditor;
import workbench.gui.renderer.RendererSetup;
import workbench.interfaces.StatementParameterPrompter;
import workbench.interfaces.ValidatingComponent;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.sql.preparedstatement.StatementParameters;
import workbench.util.SqlUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;

/**
 * @author Thomas Kellerer
 *         <p/>
 *         An component to enter parameters for prepared statements
 */
public class ParameterEditor
    extends JPanel
    implements ValidatingComponent {
  public static final StatementParameterPrompter GUI_PROMPTER = new StatementParameterPrompter() {
    @Override
    public boolean showParameterDialog(StatementParameters parms, boolean showNames) {
      return ParameterEditor.showParameterDialog(parms, showNames);
    }
  };
  private static boolean dialogResult = false;
  private WbTable parameterTable;
  private StatementParameters parameters;
  private StatementParameterTableModel model;

  public ParameterEditor(StatementParameters parms, boolean showNames) {
    super();
    this.parameters = parms;
    this.model = new StatementParameterTableModel(this.parameters, showNames);
    this.parameterTable = new WbTable();
    this.parameterTable.setRowSelectionAllowed(false);
    this.parameterTable.setColumnSelectionAllowed(false);
    this.parameterTable.setModel(this.model);
    this.parameterTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    this.parameterTable.setRendererSetup(new RendererSetup(false));
    TableColumn col = this.parameterTable.getColumnModel().getColumn(2);
    col.setCellEditor(WbTextCellEditor.createInstance());

    JLabel l = new JLabel(ResourceMgr.getString("TxtPSParameterInputText"));
    Border b = BorderFactory.createEmptyBorder(5, 2, 5, 2);
    l.setBorder(b);
    l.setBackground(Color.WHITE);
    l.setOpaque(true);
    l.setHorizontalAlignment(SwingConstants.LEFT);

    this.setLayout(new BorderLayout());

    JScrollPane scroll = new JScrollPane(this.parameterTable);
    b = BorderFactory.createEmptyBorder(5, 0, 0, 0);
    Border b2 = BorderFactory.createCompoundBorder(b, scroll.getBorder());
    scroll.setBorder(b2);

    this.add(l, BorderLayout.NORTH);
    this.add(scroll, BorderLayout.CENTER);
  }

  public static synchronized boolean showParameterDialog(final StatementParameters parms, final boolean showNames) {
    WbSwingUtilities.invoke(new Runnable() {
      @Override
      public void run() {
        ParameterEditor editor = new ParameterEditor(parms, showNames);
        Dimension d = new Dimension(300, 250);
        editor.setMinimumSize(d);
        editor.setPreferredSize(d);

        dialogResult = false;
        Frame parent = WbManager.getInstance().getCurrentWindow();
        boolean ok = ValidatingDialog.showConfirmDialog(parent, editor, ResourceMgr.getString("TxtEditPSParameterWindowTitle"));
        if (ok) {
          try {
            editor.applyValues();
            dialogResult = true;
          } catch (Exception e) {
            LogMgr.logError("VariablesEditor.showVariablesDialog()", "Error when saving values", e);
            dialogResult = false;
          }
        }
      }
    });
    return dialogResult;
  }

  public void applyValues() {
    this.parameterTable.stopEditing();
    int count = this.parameters.getParameterCount();
    for (int i = 0; i < count; i++) {
      String v = this.model.getParameterValue(i);
      this.parameters.setParameterValue(i, v);
    }
  }

  @Override
  public void componentDisplayed() {
    int count = parameterTable.getColumnCount();
    this.parameterTable.setColumnSelectionInterval(count - 1, count - 1);
    this.parameterTable.editCellAt(0, count - 1);
    TableCellEditor editor = this.parameterTable.getCellEditor();
    if (editor instanceof WbTextCellEditor) {
      WbTextCellEditor wbedit = (WbTextCellEditor) editor;
      wbedit.selectAll();
      wbedit.requestFocus();
    }
  }

  @Override
  public boolean validateInput() {
    this.parameterTable.stopEditing();
    int count = this.parameters.getParameterCount();
    for (int i = 0; i < count; i++) {
      String v = this.model.getParameterValue(i);
      if (!this.parameters.isValueValid(i, v)) {
        String error = ResourceMgr.getString("ErrInvalidPSParameterValue").replace("%value%", v);
        error = error.replace("%type%", SqlUtil.getTypeName(this.parameters.getParameterType(i)));
        WbSwingUtilities.showErrorMessage(this, error);
        return false;
      }
    }
    return true;
  }

}
