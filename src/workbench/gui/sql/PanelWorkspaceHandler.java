/*
 * PanelWorkspaceHandler.java
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
package workbench.gui.sql;

import workbench.gui.settings.ExternalFileHandling;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.WbProperties;
import workbench.util.WbWorkspace;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Thomas Kellerer
 */
public class PanelWorkspaceHandler {
  private SqlPanel client;

  public PanelWorkspaceHandler(SqlPanel panel) {
    client = panel;
  }

  public void readFromWorkspace(WbWorkspace w, int index) {
    if (client.hasFileLoaded()) {
      client.closeFile(true, false);
    }
    client.reset();

    try {
      w.readHistoryData(index, client.sqlHistory);
    } catch (Exception e) {
      LogMgr.logWarning("SqlPanel.readFromWorkspace()", "Could not read history data for index=" + index);
      client.clearSqlHistory(false);
    }

    client.setTabName(w.getTabTitle(index));

    int v = w.getMaxRows(index);
    client.statusBar.setMaxRows(v);
    v = w.getQueryTimeout(index);
    client.statusBar.setQueryTimeout(v);

    String filename = w.getExternalFileName(index);
    boolean fileLoaded = false;
    if (filename != null) {
      String encoding = w.getExternalFileEncoding(index);
      fileLoaded = client.readFile(filename, encoding);
    }

    if (!fileLoaded) {
      try {
        client.sqlHistory.showCurrent();
      } catch (Exception e) {
        LogMgr.logError("PanelWorkspaceHandler.readFromWorkspace()", "Error when showing current history entry", e);
      }
    } else {
      int cursorPos = w.getExternalFileCursorPos(index);
      if (cursorPos > -1 && cursorPos < client.editor.getText().length()) client.editor.setCaretPosition(cursorPos);
    }

    WbProperties props = w.getSettings();

    int loc = props.getIntProperty("tab" + (index) + ".divider.location", 200);
    client.setDividerLocation(loc);
    loc = props.getIntProperty("tab" + (index) + ".divider.lastlocation", 0);
    if (loc > 0) client.contentPanel.setLastDividerLocation(loc);

    boolean appendResults = props.getBoolProperty("tab" + (index) + ".append.results", false);
    boolean locked = props.getBoolProperty("tab" + (index) + ".locked", false);
    client.setLocked(locked);
    client.setAppendResults(appendResults);
    client.updateAppendAction();
    client.editor.clearUndoBuffer();
    client.editor.resetModified();
  }


  public void saveToWorkspace(WbWorkspace w, int index)
      throws IOException {
    if (!client.hasFileLoaded() ||
        client.hasFileLoaded() && Settings.getInstance().getFilesInWorkspaceHandling() != ExternalFileHandling.none) {
      client.storeStatementInHistory(); // make sure the current content is stored in the SqlHistory object
      w.addHistoryEntry(index, client.getHistory());
    }
    Properties props = w.getSettings();

    int location = client.contentPanel.getDividerLocation();
    int last = client.contentPanel.getLastDividerLocation();
    props.setProperty("tab" + (index) + ".divider.location", Integer.toString(location));
    props.setProperty("tab" + (index) + ".divider.lastlocation", Integer.toString(last));
    props.setProperty("tab" + (index) + ".append.results", Boolean.toString(client.getAppendResults()));
    props.setProperty("tab" + (index) + ".locked", Boolean.toString(client.isLocked()));
    props.setProperty("tab" + index + ".type", PanelType.sqlPanel.toString());

    w.setMaxRows(index, client.statusBar.getMaxRows());
    w.setQueryTimeout(index, client.statusBar.getQueryTimeout());
    if (client.hasFileLoaded() && Settings.getInstance().getFilesInWorkspaceHandling() == ExternalFileHandling.link) {
      w.setExternalFileName(index, client.getCurrentFileName());
      w.setExternalFileCursorPos(index, client.editor.getCaretPosition());
      w.setExternalFileEncoding(index, client.editor.getCurrentFileEncoding());
    }

    String title = client.getTabName();
    if (title == null) {
      title = ResourceMgr.getDefaultTabLabel();
    }
    w.setTabTitle(index, title);
  }

}
