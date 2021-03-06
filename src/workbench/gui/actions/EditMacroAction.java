/*
 * RunMacroAction.java
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
import workbench.gui.macros.MacroDefinitionPanel;
import workbench.resource.Settings;
import workbench.sql.macros.MacroDefinition;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Thomas Kellerer
 */
public class EditMacroAction
    extends WbAction {
  private final String windowKey = "workbench.gui.macroeditor";
  private MacroDefinition macro;

  public EditMacroAction() {
    super();
    setMenuTextByKey("LblEditMacro");
    this.setIcon(null);
    setEnabled(false);
  }

  public void setMacro(MacroDefinition def) {
    this.macro = def;
    setEnabled(macro != null);
  }

  @Override
  public void executeAction(ActionEvent e) {
    if (this.macro == null) return;
    JFrame frame = WbManager.getInstance().getCurrentWindow();
    MacroDefinitionPanel panel = new MacroDefinitionPanel(null);
    MacroDefinition editMacro = macro.createCopy();
    panel.setMacro(editMacro);
    ValidatingDialog dialog = new ValidatingDialog(frame, "Edit macro", panel);
    if (!Settings.getInstance().restoreWindowSize(dialog, windowKey)) {
      dialog.setSize(650, 500);
    }
    WbSwingUtilities.center(dialog, frame);
    dialog.setVisible(true);
    Settings.getInstance().storeWindowSize(dialog, windowKey);
    if (!dialog.isCancelled()) {
      panel.applyChanges();
      editMacro.copyTo(macro);
    }
  }
}
