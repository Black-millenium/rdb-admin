/*
 * XmlOptionsPanel.java
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
package workbench.gui.dialogs.export;

import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import javax.swing.*;
import java.awt.*;

/**
 * @author Thomas Kellerer
 */
public class XmlOptionsPanel
    extends JPanel
    implements XmlOptions {

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JCheckBox useCdata;
  private JCheckBox verboseXmlCheckBox;
  private ButtonGroup versionGroup;
  private JRadioButton xml10;
  private JRadioButton xml11;

  public XmlOptionsPanel() {
    super();
    initComponents();
  }

  public void saveSettings() {
    Settings s = Settings.getInstance();
    s.setProperty("workbench.export.xml.usecdata", this.getUseCDATA());
    s.setProperty("workbench.export.xml.verbosexml", this.getUseVerboseXml());
    s.setProperty("workbench.export.xml.xmlversion", getXMLVersion());
  }

  public void restoreSettings() {
    Settings s = Settings.getInstance();
    this.setUseCDATA(s.getBoolProperty("workbench.export.xml.usecdata"));
    this.setUseVerboseXml(s.getBoolProperty("workbench.export.xml.verbosexml", true));
    String version = s.getProperty("workbench.export.xml.xmlversion", s.getDefaultXmlVersion());
    if (version.equals("1.0")) {
      xml10.setSelected(true);
    } else if (version.equals("1.1")) {
      xml11.setSelected(true);
    }
  }

  @Override
  public String getXMLVersion() {
    if (xml11.isSelected()) {
      return "1.1";
    }
    return "1.0";
  }

  @Override
  public boolean getUseVerboseXml() {
    return this.verboseXmlCheckBox.isSelected();
  }

  @Override
  public void setUseVerboseXml(boolean flag) {
    this.verboseXmlCheckBox.setSelected(flag);
  }

  @Override
  public boolean getUseCDATA() {
    return useCdata.isSelected();
  }

  @Override
  public void setUseCDATA(boolean flag) {
    useCdata.setSelected(flag);
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    GridBagConstraints gridBagConstraints;

    versionGroup = new ButtonGroup();
    useCdata = new JCheckBox();
    verboseXmlCheckBox = new JCheckBox();
    xml10 = new JRadioButton();
    xml11 = new JRadioButton();

    setLayout(new GridBagLayout());

    useCdata.setText(ResourceMgr.getString("LblExportUseCDATA")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    add(useCdata, gridBagConstraints);

    verboseXmlCheckBox.setText(ResourceMgr.getString("LblExportVerboseXml")); // NOI18N
    verboseXmlCheckBox.setToolTipText(ResourceMgr.getString("d_LblExportVerboseXml")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    add(verboseXmlCheckBox, gridBagConstraints);

    versionGroup.add(xml10);
    xml10.setSelected(true);
    xml10.setText("XML 1.0");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    add(xml10, gridBagConstraints);

    versionGroup.add(xml11);
    xml11.setText("XML 1.1");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    add(xml11, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // End of variables declaration//GEN-END:variables

}
