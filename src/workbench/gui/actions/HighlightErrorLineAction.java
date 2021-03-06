/*
 * HighlightCurrentStatement.java
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

import workbench.resource.GuiSettings;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Toggle highlighting of the the error line (or statement).
 *
 * @author Thomas Kellerer
 */
public class HighlightErrorLineAction
    extends CheckBoxAction
    implements PropertyChangeListener {

  public HighlightErrorLineAction() {
    super("LblHiliteErr", GuiSettings.PROPERTY_HILITE_ERROR_LINE);
    this.setMenuItemName(ResourceMgr.MNU_TXT_SQL);
    Settings.getInstance().addPropertyChangeListener(this, GuiSettings.PROPERTY_HILITE_ERROR_LINE);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    this.setSwitchedOn(Settings.getInstance().getBoolProperty(GuiSettings.PROPERTY_HILITE_ERROR_LINE, false));
  }

}
