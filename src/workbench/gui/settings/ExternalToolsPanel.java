/*
 * ExternalToolsPanel.java
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
package workbench.gui.settings;

import workbench.gui.actions.DeleteListEntryAction;
import workbench.gui.actions.NewListEntryAction;
import workbench.gui.components.DividerBorder;
import workbench.gui.components.WbToolbar;
import workbench.interfaces.FileActions;
import workbench.interfaces.Restoreable;
import workbench.resource.Settings;
import workbench.util.ToolDefinition;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Thomas Kellerer
 */
public class ExternalToolsPanel
    extends JPanel
    implements Restoreable, ListSelectionListener, FileActions,
    PropertyChangeListener {
  private JList toolList;
  private ToolDefinitionPanel definitionPanel;
  private WbToolbar toolbar;
  private DefaultListModel tools;

  public ExternalToolsPanel() {
    super();
    setLayout(new BorderLayout());

    toolList = new JList();
    toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    toolList.setBorder(new EmptyBorder(2, 1, 2, 1));

    JScrollPane scroll = new JScrollPane(toolList);

    this.toolbar = new WbToolbar();
    this.toolbar.add(new NewListEntryAction(this));
    this.toolbar.add(new DeleteListEntryAction(this));
    toolbar.setBorder(DividerBorder.BOTTOM_DIVIDER);

    definitionPanel = new ToolDefinitionPanel();

    add(toolbar, BorderLayout.NORTH);
    add(scroll, BorderLayout.WEST);
    add(definitionPanel, BorderLayout.CENTER);
  }

  @Override
  public void saveSettings() {
    List<ToolDefinition> l = new LinkedList<ToolDefinition>();
    Enumeration e = this.tools.elements();
    while (e.hasMoreElements()) {
      l.add((ToolDefinition) e.nextElement());
    }
    Settings.getInstance().setExternalTools(l);
  }

  @Override
  public void restoreSettings() {
    tools = new DefaultListModel();
    List<ToolDefinition> t = Settings.getInstance().getAllExternalTools();
    for (ToolDefinition tool : t) {
      tools.addElement(tool);
    }
    toolList.setModel(tools);
    toolList.addListSelectionListener(this);
    toolList.setSelectedIndex(0);
  }

  @Override
  public void valueChanged(ListSelectionEvent evt) {
    ToolDefinition def = (ToolDefinition) toolList.getSelectedValue();
    definitionPanel.setDefinition(def);
  }

  @Override
  public void saveItem() throws Exception {
  }

  @Override
  public void deleteItem() throws Exception {
    int index = toolList.getSelectedIndex();
    if (index > -1) {
      tools.remove(index);
    }
    if (toolList.getModel().getSize() == 0) {
      definitionPanel.setDefinition(null);
    }
    toolList.repaint();
  }

  @Override
  public void newItem(boolean copyCurrent) throws Exception {
    try {
      ToolDefinition tool = new ToolDefinition("path_to_program", "commandline parameters", "New Tool");
      tools.addElement(tool);
      toolList.setSelectedIndex(tools.size() - 1);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals("name")) {
      toolList.repaint();
    }
  }

}
