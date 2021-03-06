/*
 * FileExitAction.java
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
import workbench.resource.ResourceMgr;

import java.awt.event.ActionEvent;

/**
 * Exit and close the application.
 *
 * @author Thomas Kellerer
 * @see workbench.WbManager#exitWorkbench(boolean)
 */
public class FileExitAction
    extends WbAction {
  public FileExitAction() {
    super();
    this.initMenuDefinition("MnuTxtExit");
  }

  @Override
  public void executeAction(ActionEvent e) {
    boolean forceShutdown = false;
    if (isCtrlPressed(e)) {
      forceShutdown = WbSwingUtilities.getYesNo(WbManager.getInstance().getCurrentWindow(), ResourceMgr.getString("MsgAbortWarning"));
    }
    WbManager.getInstance().exitWorkbench(forceShutdown);
  }
}
