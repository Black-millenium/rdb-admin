/*
 * MainPanel.java
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
package workbench.interfaces;

import workbench.db.WbConnection;
import workbench.gui.actions.WbAction;
import workbench.gui.bookmarks.NamedScriptLocation;
import workbench.gui.components.WbToolbar;
import workbench.util.WbWorkspace;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * @author Thomas Kellerer
 */
public interface MainPanel
    extends ResultLogger {
  List getMenuItems();

  WbToolbar getToolbar();

  void showLogPanel();

  void showResultPanel();

  void setConnectionClient(Connectable client);

  WbConnection getConnection();

  void setConnection(WbConnection aConnection);

  void addToToolbar(WbAction anAction, boolean aFlag);

  void setFont(Font aFont);

  void disconnect();

  String getTabTitle();

  void setTabTitle(JTabbedPane tab, int index);

  void setTabName(String name);

  String getId();

  boolean isConnected();

  boolean isBusy();

  boolean isCancelling();

  void dispose();

  void panelSelected();

  void readFromWorkspace(WbWorkspace w, int index) throws IOException;

  void saveToWorkspace(WbWorkspace w, int index) throws IOException;

  boolean canClosePanel(boolean checkTransactions);

  boolean isModified();

  void reset();

  boolean isLocked();

  void setLocked(boolean flag);

  List<NamedScriptLocation> getBookmarks();

  boolean isModifiedAfter(long time);

  void jumpToBookmark(NamedScriptLocation bookmark);

  boolean supportsBookmarks();
}
