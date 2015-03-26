/*
 * ReplacePanel.java
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
package workbench.gui.components;

import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.EscAction;
import workbench.interfaces.Replaceable;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.ExceptionUtil;
import workbench.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowListener;

/**
 * @author Thomas Kellerer
 */
public class ReplacePanel
    extends JPanel
    implements ActionListener, WindowListener, FocusListener {
  // Variables declaration - do not modify//GEN-BEGIN:variables
  protected javax.swing.JButton closeButton;
  protected javax.swing.JLabel criteriaLabel;
  protected javax.swing.JButton findButton;
  protected javax.swing.JButton findNextButton;
  protected javax.swing.JCheckBox ignoreCaseCheckBox;
  protected javax.swing.JPanel jPanel1;
  protected javax.swing.JButton replaceAllButton;
  protected javax.swing.JLabel replaceLabel;
  protected javax.swing.JButton replaceNextButton;
  protected javax.swing.JComboBox replaceValue;
  protected javax.swing.JComboBox searchCriteria;
  protected javax.swing.JCheckBox selectedTextCheckBox;
  protected javax.swing.JPanel spacerPanel;
  protected javax.swing.JCheckBox useRegexCheckBox;
  protected javax.swing.JCheckBox wordsOnlyCheckBox;
  protected javax.swing.JCheckBox wrapSearchCbx;
  private String settingsKey;
  private String caseProperty;
  private String wordProperty;
  private String selectedProperty;
  private String regexProperty;
  private String criteriaProperty;
  private String replacementProperty;
  private String wrapProperty;
  private Replaceable client;
  private int lastPos = -1;
  private JDialog dialog;
  private EscAction escAction;

  public ReplacePanel(Replaceable aClient) {
    this(aClient, "workbench.sql.replace", null);
  }

  public ReplacePanel(Replaceable aClient, String key, String selectedText) {
    super();
    initComponents();
    this.client = aClient;
    settingsKey = key;
    caseProperty = settingsKey + ".ignoreCase";
    wordProperty = settingsKey + ".wholeWord";
    selectedProperty = settingsKey + ".selectedText";
    regexProperty = settingsKey + ".useRegEx";
    wrapProperty = settingsKey + ".wrapSearch";
    criteriaProperty = "criteria";
    replacementProperty = "replacement";

    WbTraversalPolicy policy = new WbTraversalPolicy();
    policy.addComponent(this.searchCriteria.getEditor().getEditorComponent());
    policy.addComponent(this.replaceValue.getEditor().getEditorComponent());
    policy.addComponent(this.ignoreCaseCheckBox);
    policy.addComponent(this.wordsOnlyCheckBox);
    policy.addComponent(this.useRegexCheckBox);
    policy.addComponent(this.selectedTextCheckBox);
    policy.addComponent(this.wrapSearchCbx);
    policy.addComponent(this.findButton);
    policy.addComponent(this.replaceNextButton);
    policy.addComponent(this.replaceAllButton);
    policy.addComponent(this.closeButton);
    policy.setDefaultComponent(searchCriteria.getEditor().getEditorComponent());
    this.setFocusTraversalPolicy(policy);

    this.findButton.addActionListener(this);
    this.replaceNextButton.addActionListener(this);
    this.replaceAllButton.addActionListener(this);
    this.closeButton.addActionListener(this);
    this.findNextButton.addActionListener(this);

    this.replaceNextButton.setEnabled(false);
    this.findNextButton.setEnabled(false);

    ((HistoryTextField) searchCriteria).setColumns(30);
    ((HistoryTextField) searchCriteria).setSettingsProperty(criteriaProperty);
    ((HistoryTextField) replaceValue).setColumns(30);
    ((HistoryTextField) replaceValue).setSettingsProperty(replacementProperty);

    this.restoreSettings();

    if (selectedText != null) {
      this.selectedTextCheckBox.setText(selectedText);
    }

    replaceValue.getEditor().getEditorComponent().addFocusListener(this);
    searchCriteria.getEditor().getEditorComponent().addFocusListener(this);
  }

  @Override
  public void focusLost(FocusEvent e) {
  }

  @Override
  public void focusGained(FocusEvent e) {
    if (e.getComponent() == replaceValue.getEditor().getEditorComponent()) {
      replaceValue.getEditor().selectAll();
    } else if (e.getComponent() == searchCriteria.getEditor().getEditorComponent()) {
      searchCriteria.getEditor().selectAll();
    }
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

    criteriaLabel = new javax.swing.JLabel();
    replaceLabel = new javax.swing.JLabel();
    spacerPanel = new javax.swing.JPanel();
    findButton = new WbButton();
    findNextButton = new WbButton();
    replaceNextButton = new WbButton();
    replaceAllButton = new WbButton();
    closeButton = new WbButton();
    replaceValue = new HistoryTextField();
    searchCriteria = new HistoryTextField();
    jPanel1 = new javax.swing.JPanel();
    ignoreCaseCheckBox = new javax.swing.JCheckBox();
    wordsOnlyCheckBox = new javax.swing.JCheckBox();
    useRegexCheckBox = new javax.swing.JCheckBox();
    selectedTextCheckBox = new javax.swing.JCheckBox();
    wrapSearchCbx = new javax.swing.JCheckBox();

    setFocusCycleRoot(true);
    setLayout(new java.awt.GridBagLayout());

    criteriaLabel.setText(ResourceMgr.getString("LblSearchCriteria")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
    add(criteriaLabel, gridBagConstraints);

    replaceLabel.setText(ResourceMgr.getString("LblReplaceNewValue")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 8, 0, 0);
    add(replaceLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.weighty = 1.0;
    add(spacerPanel, gridBagConstraints);

    findButton.setText(ResourceMgr.getString("LblFindNow")); // NOI18N
    findButton.setToolTipText(ResourceMgr.getString("d_LblFindNow")); // NOI18N
    findButton.setName("findbutton"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
    add(findButton, gridBagConstraints);

    findNextButton.setText(ResourceMgr.getString("LblFindNext")); // NOI18N
    findNextButton.setToolTipText(ResourceMgr.getString("d_LblFindNext")); // NOI18N
    findNextButton.setName("findnextbutton"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 5);
    add(findNextButton, gridBagConstraints);

    replaceNextButton.setText(ResourceMgr.getString("LblReplaceNext")); // NOI18N
    replaceNextButton.setToolTipText(ResourceMgr.getString("d_LblReplaceNext")); // NOI18N
    replaceNextButton.setName("replacenextbutton"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 5);
    add(replaceNextButton, gridBagConstraints);

    replaceAllButton.setText(ResourceMgr.getString("LblReplaceAll")); // NOI18N
    replaceAllButton.setToolTipText(ResourceMgr.getString("d_LblReplaceAll")); // NOI18N
    replaceAllButton.setName("replaceallbutton"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 5);
    add(replaceAllButton, gridBagConstraints);

    closeButton.setText(ResourceMgr.getString("LblClose")); // NOI18N
    closeButton.setName("closebutton"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
    add(closeButton, gridBagConstraints);

    replaceValue.setName("replacetext"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 8);
    add(replaceValue, gridBagConstraints);

    searchCriteria.setName("searchtext"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 8);
    add(searchCriteria, gridBagConstraints);

    jPanel1.setLayout(new java.awt.GridBagLayout());

    ignoreCaseCheckBox.setText(ResourceMgr.getString("LblSearchIgnoreCase")); // NOI18N
    ignoreCaseCheckBox.setToolTipText(ResourceMgr.getString("d_LblSearchIgnoreCase")); // NOI18N
    ignoreCaseCheckBox.setBorder(null);
    ignoreCaseCheckBox.setName("ignorecase"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    jPanel1.add(ignoreCaseCheckBox, gridBagConstraints);

    wordsOnlyCheckBox.setText(ResourceMgr.getString("LblSearchWordsOnly")); // NOI18N
    wordsOnlyCheckBox.setToolTipText(ResourceMgr.getString("d_LblSearchWordsOnly")); // NOI18N
    wordsOnlyCheckBox.setBorder(null);
    wordsOnlyCheckBox.setName("wordsonly"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
    jPanel1.add(wordsOnlyCheckBox, gridBagConstraints);

    useRegexCheckBox.setText(ResourceMgr.getString("LblSearchRegEx")); // NOI18N
    useRegexCheckBox.setToolTipText(ResourceMgr.getString("d_LblSearchRegEx")); // NOI18N
    useRegexCheckBox.setBorder(null);
    useRegexCheckBox.setName("regex"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
    jPanel1.add(useRegexCheckBox, gridBagConstraints);

    selectedTextCheckBox.setText(ResourceMgr.getString("LblSelectedTextOnly")); // NOI18N
    selectedTextCheckBox.setToolTipText(ResourceMgr.getString("d_LblSelectedTextOnly")); // NOI18N
    selectedTextCheckBox.setBorder(null);
    selectedTextCheckBox.setName("selectedtext"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
    jPanel1.add(selectedTextCheckBox, gridBagConstraints);

    wrapSearchCbx.setText(ResourceMgr.getString("LblSearchWrap")); // NOI18N
    wrapSearchCbx.setToolTipText(ResourceMgr.getString("d_LblSearchWrap")); // NOI18N
    wrapSearchCbx.setBorder(null);
    wrapSearchCbx.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        wrapSearchCbxActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 28, 0, 0);
    jPanel1.add(wrapSearchCbx, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridheight = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(12, 7, 0, 0);
    add(jPanel1, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  private void wrapSearchCbxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_wrapSearchCbxActionPerformed
  {//GEN-HEADEREND:event_wrapSearchCbxActionPerformed
    this.client.setWrapSearch(wrapSearchCbx.isSelected());
  }//GEN-LAST:event_wrapSearchCbxActionPerformed

  public void showReplaceDialog(Component caller, final String selectedText) {
    showReplaceDialog(caller, selectedText, ResourceMgr.getString("TxtWindowTitleReplaceText"));
  }

  public void showReplaceDialog(Component caller, final String selectedText, String title) {
    if (this.dialog != null) {
      this.dialog.setVisible(true);
      this.dialog.requestFocus();
      return;
    }
    try {
      Window w = WbSwingUtilities.getWindowAncestor(caller);

      this.dialog = null;

      if (w instanceof Frame) {
        this.dialog = new JDialog((Frame) w);
      } else if (w instanceof Dialog) {
        this.dialog = new JDialog((Dialog) w);
      }
      this.dialog.setTitle(title);
      this.dialog.getContentPane().add(this);
      this.dialog.pack();
      this.dialog.setResizable(false);
      if (!Settings.getInstance().restoreWindowPosition(this.dialog, settingsKey + ".window")) {
        WbSwingUtilities.center(dialog, w);
      }
      this.dialog.addWindowListener(this);

      boolean hasSelectedText = false;

      if (!StringUtil.isEmptyString(selectedText) && selectedText.indexOf('\n') == -1 && selectedText.indexOf('\r') == -1) {
        ((HistoryTextField) searchCriteria).setText(selectedText);
        hasSelectedText = true;
      }

      escAction = new EscAction(dialog, this);

      final boolean criteriaAdded = hasSelectedText;

      EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (criteriaAdded) {
            ((HistoryTextField) replaceValue).selectAll();
            ((HistoryTextField) replaceValue).requestFocus();
          } else {
            ((HistoryTextField) searchCriteria).selectAll();
            ((HistoryTextField) searchCriteria).requestFocus();
          }
        }
      });
      this.dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void actionPerformed(java.awt.event.ActionEvent e) {
    Object source = e.getSource();
    updateHistoryFields();
    if (source == this.findButton) {
      this.findFirst();
    } else if (source == this.findNextButton) {
      findNext();
    } else if (source == this.replaceNextButton) {
      this.replaceNext();
    } else if (source == this.replaceAllButton) {
      this.replaceAll();
    } else if (source == this.closeButton || e.getActionCommand().equals(escAction.getActionName())) {
      this.closeWindow();
    }
  }

  private void findNext() {
    try {
      this.lastPos = this.client.findNext();
      this.replaceNextButton.setEnabled(this.lastPos > -1);
      this.findNextButton.setEnabled(this.lastPos > -1);
    } catch (Exception e) {
      WbSwingUtilities.showErrorMessage(this, ExceptionUtil.getDisplay(e));
    }
  }

  private void findFirst() {
    String toFind = ((HistoryTextField) searchCriteria).getText();
    try {
      this.lastPos = this.client.findFirst(toFind, this.ignoreCaseCheckBox.isSelected(), this.wordsOnlyCheckBox.isSelected(), this.useRegexCheckBox.isSelected());
      this.replaceNextButton.setEnabled(this.lastPos > -1);
      this.findNextButton.setEnabled(this.lastPos > -1);
    } catch (Exception e) {
      WbSwingUtilities.showErrorMessage(this, ExceptionUtil.getDisplay(e));
    }
  }

  private void replaceNext() {
    if (this.lastPos < 0) this.findFirst();

    if (this.client.replaceCurrent(((HistoryTextField) replaceValue).getText(), this.useRegexCheckBox.isSelected())) {
      this.findNext();
    } else {
      this.replaceNextButton.setEnabled(false);
    }
  }

  private void replaceAll() {
    boolean selected = this.selectedTextCheckBox.isEnabled() && this.selectedTextCheckBox.isSelected();
    this.client.replaceAll(((HistoryTextField) searchCriteria).getText(),
        ((HistoryTextField) replaceValue).getText(),
        selected,
        this.ignoreCaseCheckBox.isSelected(),
        this.wordsOnlyCheckBox.isSelected(),
        this.useRegexCheckBox.isSelected());
  }

  private void closeWindow() {
    if (this.dialog != null) {
      this.saveSettings();
      this.escAction = null;
      this.dialog.setVisible(false);
      this.dialog.dispose();
      this.dialog = null;
    }
  }

  private void updateHistoryFields() {
    ((HistoryTextField) searchCriteria).storeCurrent();
    ((HistoryTextField) replaceValue).storeCurrent();
  }

  private void saveSettings() {
    Settings.getInstance().setProperty(caseProperty, Boolean.toString(this.ignoreCaseCheckBox.isSelected()));
    Settings.getInstance().setProperty(wordProperty, Boolean.toString(this.wordsOnlyCheckBox.isSelected()));
    Settings.getInstance().setProperty(selectedProperty, Boolean.toString(this.selectedTextCheckBox.isSelected()));
    Settings.getInstance().setProperty(regexProperty, Boolean.toString(this.useRegexCheckBox.isSelected()));
    Settings.getInstance().setProperty(wrapProperty, Boolean.toString(this.wrapSearchCbx.isSelected()));
    ((HistoryTextField) searchCriteria).saveSettings(Settings.getInstance(), settingsKey + ".");
    ((HistoryTextField) replaceValue).saveSettings(Settings.getInstance(), settingsKey + ".");
    Settings.getInstance().storeWindowPosition(this.dialog, settingsKey + ".window");
  }

  private void restoreSettings() {
    this.ignoreCaseCheckBox.setSelected(Settings.getInstance().getBoolProperty(caseProperty, true));
    this.wordsOnlyCheckBox.setSelected(Settings.getInstance().getBoolProperty(wordProperty, false));
    this.selectedTextCheckBox.setSelected(Settings.getInstance().getBoolProperty(selectedProperty, false));
    this.useRegexCheckBox.setSelected(Settings.getInstance().getBoolProperty(regexProperty, true));
    this.wrapSearchCbx.setSelected(Settings.getInstance().getBoolProperty(wrapProperty, false));
    ((HistoryTextField) searchCriteria).restoreSettings(Settings.getInstance(), settingsKey + ".");
    ((HistoryTextField) replaceValue).restoreSettings(Settings.getInstance(), settingsKey + ".");
  }

  @Override
  public void windowActivated(java.awt.event.WindowEvent e) {
  }

  @Override
  public void windowClosed(java.awt.event.WindowEvent e) {
  }

  @Override
  public void windowClosing(java.awt.event.WindowEvent e) {
    this.closeWindow();
  }

  @Override
  public void windowDeactivated(java.awt.event.WindowEvent e) {
  }

  @Override
  public void windowDeiconified(java.awt.event.WindowEvent e) {
  }

  @Override
  public void windowIconified(java.awt.event.WindowEvent e) {
  }

  @Override
  public void windowOpened(java.awt.event.WindowEvent e) {
  }
  // End of variables declaration//GEN-END:variables


}
