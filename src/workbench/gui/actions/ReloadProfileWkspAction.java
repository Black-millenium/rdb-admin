/*
 * AssignWorkspaceAction.java
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

import workbench.db.ConnectionProfile;
import workbench.gui.MainWindow;
import workbench.resource.ResourceMgr;
import workbench.util.StringUtil;

import java.awt.event.ActionEvent;

/**
 * Action to (re)load the workspace assigned to the current connection profile
 *
 * @author Thomas Kellerer
 * @see workbench.gui.MainWindow#assignWorkspace()
 * @see workbench.db.ConnectionProfile
 * @see workbench.util.WbWorkspace
 */
public class ReloadProfileWkspAction
    extends WbAction {
  private MainWindow client;

  public ReloadProfileWkspAction(MainWindow aClient) {
    super();
    this.client = aClient;
    this.initMenuDefinition("MnuTxtLoadProfileWksp", null);
    this.setMenuItemName(ResourceMgr.MNU_TXT_WORKSPACE);
    this.setIcon(null);
  }

  @Override
  public void executeAction(ActionEvent e) {
    String file = getWorkspace();
    if (file != null) {
      this.client.loadCurrentProfileWorkspace();
    }
  }

  private String getWorkspace() {
    ConnectionProfile profile = this.client.getCurrentProfile();
    if (profile != null) {
      String workspaceFile = profile.getWorkspaceFile();
      if (StringUtil.isNonEmpty(workspaceFile)) {
        return workspaceFile;
      }
    }
    return null;
  }
}
