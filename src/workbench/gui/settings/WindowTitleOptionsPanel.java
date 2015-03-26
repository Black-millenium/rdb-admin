/*
 * WindowTitleOptionsPanel.java
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

import workbench.gui.components.WbTraversalPolicy;
import workbench.resource.GuiSettings;
import workbench.resource.ResourceMgr;

/**
 * @author Thomas Kellerer
 */
public class WindowTitleOptionsPanel
    extends javax.swing.JPanel
    implements workbench.interfaces.Restoreable {

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox encloseChar;
  private javax.swing.JLabel encloseCharLabel;
  private javax.swing.JCheckBox includeUser;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JCheckBox productAtEnd;
  private javax.swing.JCheckBox showProfileGroup;
  private javax.swing.JCheckBox showUrl;
  private javax.swing.JCheckBox showWorkspace;
  private javax.swing.JTextField titleGroupSep;
  private javax.swing.JLabel titleGroupSepLabel;
  private javax.swing.JComboBox windowTitleComboBox;
  private javax.swing.JLabel windowTitleLabel;
  public WindowTitleOptionsPanel() {
    super();
    initComponents();
    // It is important to add these in the correct order
    // which is defined by the numeric values from Settings.SHOW_NO_FILENAME
    // SHOW_FILENAME and SHOW_FULL_PATH
    this.windowTitleComboBox.addItem(ResourceMgr.getString("TxtShowNone"));
    this.windowTitleComboBox.addItem(ResourceMgr.getString("TxtShowName"));
    this.windowTitleComboBox.addItem(ResourceMgr.getString("TxtShowPath"));

    WbTraversalPolicy policy = new WbTraversalPolicy();
    policy.addComponent(productAtEnd);
    policy.addComponent(showProfileGroup);
    policy.addComponent(showWorkspace);
    policy.addComponent(windowTitleComboBox);
    policy.setDefaultComponent(productAtEnd);

    this.encloseChar.insertItemAt(ResourceMgr.getString("TxtNothingItem"), 0);
    this.setFocusTraversalPolicy(policy);
    this.setFocusCycleRoot(false);
    this.restoreSettings();
  }

  @Override
  public final void restoreSettings() {
    int type = GuiSettings.getShowFilenameInWindowTitle();
    if (type >= GuiSettings.SHOW_NO_FILENAME && type <= GuiSettings.SHOW_FULL_PATH) {
      this.windowTitleComboBox.setSelectedIndex(type);
    }
    this.showProfileGroup.setSelected(GuiSettings.getShowProfileGroupInWindowTitle());
    this.showWorkspace.setSelected(GuiSettings.getShowWorkspaceInWindowTitle());
    this.productAtEnd.setSelected(GuiSettings.getShowProductNameAtEnd());
    this.showUrl.setSelected(GuiSettings.getShowURLinWindowTitle());
    this.includeUser.setSelected(GuiSettings.getIncludeUserInTitleURL());
    this.includeUser.setEnabled(showUrl.isSelected());
    String enclose = GuiSettings.getTitleGroupBracket();
    if (enclose == null) {
      encloseChar.setSelectedIndex(0);
    } else {
      int count = encloseChar.getItemCount();
      for (int i = 1; i < count; i++) {
        String item = (String) encloseChar.getItemAt(i);
        if (item.startsWith(enclose.trim())) {
          encloseChar.setSelectedIndex(i);
          break;
        }
      }
    }
    checkShowProfile();
    this.titleGroupSep.setText(GuiSettings.getTitleGroupSeparator());
  }

  @Override
  public void saveSettings() {
    GuiSettings.setShowFilenameInWindowTitle(this.windowTitleComboBox.getSelectedIndex());
    GuiSettings.setShowProfileGroupInWindowTitle(showProfileGroup.isSelected());
    GuiSettings.setShowWorkspaceInWindowTitle(showWorkspace.isSelected());
    GuiSettings.setShowProductNameAtEnd(productAtEnd.isSelected());
    GuiSettings.setTitleGroupSeparator(titleGroupSep.getText());
    GuiSettings.setShowURLinWindowTitle(showUrl.isSelected());
    GuiSettings.setIncludeUserInTitleURL(includeUser.isSelected());
    int index = this.encloseChar.getSelectedIndex();
    if (index == 0) {
      GuiSettings.setTitleGroupBracket(null);
    } else {
      String bracket = (String) this.encloseChar.getSelectedItem();
      GuiSettings.setTitleGroupBracket(bracket.substring(0, 1));
    }
  }

  protected void checkShowProfile() {
    this.encloseChar.setEnabled(this.showProfileGroup.isSelected());
    this.titleGroupSep.setEnabled(this.showProfileGroup.isSelected());
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    productAtEnd = new javax.swing.JCheckBox();
    showProfileGroup = new javax.swing.JCheckBox();
    showWorkspace = new javax.swing.JCheckBox();
    windowTitleLabel = new javax.swing.JLabel();
    windowTitleComboBox = new javax.swing.JComboBox();
    encloseCharLabel = new javax.swing.JLabel();
    encloseChar = new javax.swing.JComboBox();
    jPanel1 = new javax.swing.JPanel();
    titleGroupSepLabel = new javax.swing.JLabel();
    titleGroupSep = new javax.swing.JTextField();
    showUrl = new javax.swing.JCheckBox();
    includeUser = new javax.swing.JCheckBox();

    setLayout(new java.awt.GridBagLayout());

    productAtEnd.setText(ResourceMgr.getString("LblShowProductAtEnd")); // NOI18N
    productAtEnd.setToolTipText(ResourceMgr.getString("d_LblShowProductAtEnd")); // NOI18N
    productAtEnd.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 12, 2, 11);
    add(productAtEnd, gridBagConstraints);

    showProfileGroup.setText(ResourceMgr.getString("LblShowProfileGroup")); // NOI18N
    showProfileGroup.setToolTipText(ResourceMgr.getString("d_LblShowProfileGroup")); // NOI18N
    showProfileGroup.setBorder(null);
    showProfileGroup.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    showProfileGroup.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    showProfileGroup.setIconTextGap(5);
    showProfileGroup.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        showProfileGroupStateChanged(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 11);
    add(showProfileGroup, gridBagConstraints);

    showWorkspace.setText(ResourceMgr.getString("LblShowWorkspace")); // NOI18N
    showWorkspace.setToolTipText(ResourceMgr.getString("d_LblShowWorkspace")); // NOI18N
    showWorkspace.setBorder(null);
    showWorkspace.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    showWorkspace.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    showWorkspace.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 1);
    add(showWorkspace, gridBagConstraints);

    windowTitleLabel.setLabelFor(windowTitleComboBox);
    windowTitleLabel.setText(ResourceMgr.getString("LblShowEditorInfo")); // NOI18N
    windowTitleLabel.setToolTipText(ResourceMgr.getString("d_LblShowEditorInfo")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(16, 12, 0, 0);
    add(windowTitleLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(15, 4, 0, 11);
    add(windowTitleComboBox, gridBagConstraints);

    encloseCharLabel.setText(ResourceMgr.getString("LblEncloseGroupChar")); // NOI18N
    encloseCharLabel.setToolTipText(ResourceMgr.getString("d_LblEncloseGroupChar")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 12, 2, 0);
    add(encloseCharLabel, gridBagConstraints);

    encloseChar.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"( )", "{ }", "[ ]", "< >"}));
    encloseChar.setToolTipText(ResourceMgr.getDescription("LblEncloseGroupChar"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 4, 0, 11);
    add(encloseChar, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(jPanel1, gridBagConstraints);

    titleGroupSepLabel.setText(ResourceMgr.getString("LblGroupSeparator")); // NOI18N
    titleGroupSepLabel.setToolTipText(ResourceMgr.getString("d_LblGroupSeparator")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 2, 2, 0);
    add(titleGroupSepLabel, gridBagConstraints);

    titleGroupSep.setColumns(5);
    titleGroupSep.setToolTipText(ResourceMgr.getDescription("LblGroupSeparator"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 5, 2, 11);
    add(titleGroupSep, gridBagConstraints);

    showUrl.setText(ResourceMgr.getString("LblUrlInTitle")); // NOI18N
    showUrl.setToolTipText(ResourceMgr.getString("d_LblUrlInTitle")); // NOI18N
    showUrl.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    showUrl.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showUrlActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 12, 2, 11);
    add(showUrl, gridBagConstraints);

    includeUser.setText(ResourceMgr.getString("LblUrlWithUser")); // NOI18N
    includeUser.setToolTipText(ResourceMgr.getString("d_LblUrlWithUser")); // NOI18N
    includeUser.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 30, 2, 11);
    add(includeUser, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  private void showProfileGroupStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_showProfileGroupStateChanged
    checkShowProfile();
  }//GEN-LAST:event_showProfileGroupStateChanged

  private void showUrlActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_showUrlActionPerformed
  {//GEN-HEADEREND:event_showUrlActionPerformed
    includeUser.setEnabled(showUrl.isSelected());
  }//GEN-LAST:event_showUrlActionPerformed
  // End of variables declaration//GEN-END:variables
}
