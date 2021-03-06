/*
 * AddTabAction.java
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

import workbench.gui.MainWindow;
import workbench.resource.PlatformShortcuts;
import workbench.resource.ResourceMgr;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Action to add a new tab to the MainWindow's interface
 *
 * @author Thomas Kellerer
 * @see workbench.gui.MainWindow
 * @see workbench.gui.MainWindow#addTab()
 */
public class AddTabAction
    extends WbAction {
  private MainWindow client;

  public AddTabAction(MainWindow aClient) {
    super();
    this.client = aClient;
    this.setMenuItemName(ResourceMgr.MNU_TXT_VIEW);
    this.initMenuDefinition("MnuTxtAddTab", KeyStroke.getKeyStroke(KeyEvent.VK_T, PlatformShortcuts.getDefaultModifier()));
    this.setIcon(null);
  }

  @Override
  public void executeAction(ActionEvent e) {
    this.client.addTab();
  }
}
