/*
 * AboutAction.java
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
import workbench.gui.WbSwingUtilities;
import workbench.gui.dialogs.WbAboutDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Action to display program version information
 *
 * @author Thomas Kellerer
 * @see workbench.gui.dialogs.WbAboutDialog
 */
public class AboutAction
    extends WbAction {
  private MainWindow mainWindow;

  public AboutAction(MainWindow parent) {
    super();
    this.mainWindow = parent;
    initMenuDefinition("MnuTxtAbout");
    removeIcon();
  }

  @Override
  public void executeAction(ActionEvent e) {
    WbAboutDialog about = new WbAboutDialog(mainWindow);
    about.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    WbSwingUtilities.center(about, mainWindow);
    about.setVisible(true);
  }
}
