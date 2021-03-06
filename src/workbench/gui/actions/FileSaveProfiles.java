/*
 * FileSaveProfiles.java
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
import workbench.db.ConnectionMgr;
import workbench.gui.WbSwingUtilities;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.util.ExceptionUtil;

import java.awt.event.ActionEvent;

/**
 * Saves the connection profiles
 *
 * @author Thomas Kellerer
 */
public class FileSaveProfiles
    extends WbAction {
  public FileSaveProfiles() {
    super();
    this.initMenuDefinition("MnuTxtFilesSaveProfiles");
  }

  @Override
  public void executeAction(ActionEvent e) {
    try {
      ConnectionMgr.getInstance().saveProfiles();
      WbSwingUtilities.showMessage(WbManager.getInstance().getCurrentWindow(), ResourceMgr.getString("MsgProfilesSaved"));
    } catch (Exception ex) {
      LogMgr.logError("FileSaveProfiles.executeAction()", "Error saving profiles", ex);
      WbSwingUtilities.showMessage(WbManager.getInstance().getCurrentWindow(), ResourceMgr.getString("ErrSavingProfiles") + "\n" + ExceptionUtil.getDisplay(ex));
    }
  }
}
