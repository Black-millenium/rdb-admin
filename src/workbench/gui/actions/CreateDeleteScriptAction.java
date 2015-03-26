/*
 * CreateDeleteScriptAction.java
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

import workbench.db.DeleteScriptGenerator;
import workbench.db.WbConnection;
import workbench.gui.components.WbTable;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;

import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;

/**
 * Create a SQL script to delete the selected row from a WbTable.
 *
 * @author Thomas Kellerer
 * @see workbench.db.DeleteScriptGenerator
 */
public class CreateDeleteScriptAction
    extends WbAction
    implements ListSelectionListener {
  private WbTable client;

  public CreateDeleteScriptAction(WbTable aClient) {
    super();
    this.initMenuDefinition("MnuTxtCreateDeleteScript", null);
    this.setMenuItemName(ResourceMgr.MNU_TXT_DATA);
    setClient(aClient);
  }

  @Override
  public void executeAction(ActionEvent e) {
    WbConnection con = client.getDataStore().getOriginalConnection();
    if (con.isBusy()) return;

    try {
      boolean hasPK = client.checkPkColumns(true);
      if (!hasPK) return;
      DeleteScriptGenerator gen = new DeleteScriptGenerator(con);
      gen.setSource(client);
      gen.startGenerate();
    } catch (Exception ex) {
      LogMgr.logError("SqlPanel.generateDeleteScript()", "Error initializing DeleteScriptGenerator", ex);
    }
  }

  @Override
  public void valueChanged(javax.swing.event.ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    checkSelection();
  }

  private void checkSelection() {
    if (this.client == null) return;
    int rows = this.client.getSelectedRowCount();
    this.setEnabled(rows > 0);
  }

  public void setClient(WbTable w) {
    if (this.client != null) {
      this.client.getSelectionModel().removeListSelectionListener(this);
    }
    this.client = w;
    if (this.client != null) {
      this.client.getSelectionModel().addListSelectionListener(this);
      checkSelection();
    }
    this.setEnabled(this.client != null);
    checkSelection();
  }
}
