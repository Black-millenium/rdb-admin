/*
 * ShowSourceQueryAction.java
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
package workbench.gui.actions;

import workbench.WbManager;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.ValidatingDialog;
import workbench.gui.components.WbTabbedPane;
import workbench.gui.sql.EditorPanel;
import workbench.gui.sql.SqlPanel;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Thomas Kellerer
 */
public class ShowSourceQueryAction
    extends WbAction {
  private SqlPanel panel;

  public ShowSourceQueryAction(SqlPanel handler) {
    panel = handler;
    isConfigurable = false;
    initMenuDefinition("MnuTxtShowQuery");
  }

  @Override
  public boolean isEnabled() {
    return (panel != null && panel.getSourceQuery() != null);
  }

  @Override
  public void executeAction(ActionEvent e) {
    showQuery();
  }

  public void showQuery() {
    EditorPanel editor = EditorPanel.createSqlEditor();
    WbTabbedPane tab = new WbTabbedPane();

    String sql = panel.getSourceQuery();

    JPanel display = new JPanel(new BorderLayout(0, 5));

    editor.setText(sql);
    editor.setCaretPosition(0);
    editor.setEditable(false);
    Window w = SwingUtilities.getWindowAncestor(panel);
    Frame f = null;
    if (w instanceof Frame) {
      f = (Frame) w;
    } else {
      f = WbManager.getInstance().getCurrentWindow();
    }

    String loadedAt = StringUtil.formatIsoTimestamp(panel.getLoadedAt());
    String msg = ResourceMgr.getFormattedString("TxtLastExec", loadedAt);
    JLabel lbl = new JLabel(msg);
    Border etched = new EtchedBorder(EtchedBorder.LOWERED);
    lbl.setBorder(new CompoundBorder(etched, new EmptyBorder(3, 2, 2, 0)));

    display.add(editor, BorderLayout.CENTER);
    display.add(lbl, BorderLayout.NORTH);

    ResultSetInfoPanel resultInfo = new ResultSetInfoPanel(panel.getCurrentResult());

    tab.addTab("SQL", display);
    tab.addTab(ResourceMgr.getString("LblResultMeta"), resultInfo);

    ValidatingDialog d = new ValidatingDialog(f, panel.getCurrentResultTitle(), tab, false);
    if (!Settings.getInstance().restoreWindowSize(d, "workbench.resultquery.display")) {
      d.setSize(500, 350);
    }
    WbSwingUtilities.center(d, f);
    WbSwingUtilities.repaintLater(editor);
    d.setVisible(true);
    Settings.getInstance().storeWindowSize(d, "workbench.resultquery.display");
  }
}
