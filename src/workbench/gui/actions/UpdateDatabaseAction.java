/*
 * UpdateDatabaseAction.java
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

import workbench.db.WbConnection;
import workbench.interfaces.DbUpdater;
import workbench.resource.IconMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import java.awt.event.ActionEvent;

/**
 * @author Thomas Kellerer
 */
public class UpdateDatabaseAction
    extends WbAction {
  private DbUpdater panel;

  public UpdateDatabaseAction(DbUpdater aPanel) {
    super();
    this.panel = aPanel;
    this.initMenuDefinition("MnuTxtUpdateDatabase");
    this.setIcon(IconMgr.IMG_SAVE);
    this.setMenuItemName(ResourceMgr.MNU_TXT_DATA);
    this.setCreateToolbarSeparator(true);
    this.setEnabled(false);
  }

  @Override
  public void executeAction(ActionEvent e) {
    boolean confirm = Settings.getInstance().getPreviewDml();

    WbConnection connection = panel.getConnection();
    if (connection != null) {
      confirm = confirm || connection.confirmUpdatesInSession();
    }
    confirm = confirm || isCtrlPressed(e);

    panel.saveChangesToDatabase(confirm);
  }

  public void setClient(DbUpdater client) {
    this.panel = client;
  }

}
