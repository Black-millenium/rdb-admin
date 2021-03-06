/*
 * ProgressPanel.java
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
package workbench.gui.dbobjects;

import workbench.gui.WbSwingUtilities;
import workbench.gui.components.WbButton;
import workbench.interfaces.Interruptable;
import workbench.interfaces.InterruptableJob;
import workbench.resource.ResourceMgr;
import workbench.storage.RowActionMonitor;
import workbench.util.NumberStringCache;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Thomas Kellerer
 */
public class ProgressPanel
    extends JPanel
    implements RowActionMonitor {
  private Interruptable task;

  private JDialog parent;
  private int monitorType = RowActionMonitor.MONITOR_PLAIN;
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JButton cancelButton;
  private JTextField fileNameField;
  private JPanel infoPanel;
  private JTextField progressInfoText;
  private JLabel rowInfo;

  public ProgressPanel(Interruptable aWorker) {
    super();
    this.task = aWorker;
    initComponents();
    setRowSize(20);
    WbSwingUtilities.setMinimumSize(rowInfo, 20);
  }

  public void setParentDialog(JDialog d) {
    parent = d;
  }

  public void setRowInfo(long aRow) {
    this.rowInfo.setText(Long.toString(aRow));
  }

  public void setInfoText(String aText) {
    this.progressInfoText.setText(aText);
  }

  public void setObject(String name) {
    this.fileNameField.setText(name);
    updateLayout();
  }

  protected void updateLayout() {
    FontMetrics fm = this.getFontMetrics(fileNameField.getFont());
    int width = fm.stringWidth(fileNameField.getText()) + 25;
    int h = fm.getHeight() + 2;
    Dimension d = new Dimension(width, h < 22 ? 22 : h);
    this.fileNameField.setPreferredSize(d);
    this.fileNameField.setMinimumSize(d);

    if (parent != null) {
      parent.invalidate();
    }

    invalidate();
    validate();

    if (parent != null) {
      parent.validate();
      parent.pack();
    }
  }

  public void setRowSize(int cols) {
    FontMetrics fm = this.getFontMetrics(this.getFont());
    int w = fm.charWidth(' ');
    int h = fm.getHeight() + 2;
    Dimension d = new Dimension(w * cols, h < 22 ? 22 : h);
    this.rowInfo.setPreferredSize(d);
    this.rowInfo.setMinimumSize(d);
    updateLayout();
  }

  public void setInfoSize(int cols) {
    this.progressInfoText.setColumns(cols);
    this.updateLayout();
  }

  @Override
  public void jobFinished() {
  }

  @Override
  public void setCurrentObject(final String object, final long number, final long totalObjects) {
    final String info = NumberStringCache.getNumberString(number) + "/" + NumberStringCache.getNumberString(totalObjects);
    WbSwingUtilities.invoke(new Runnable() {
      @Override
      public void run() {
        if (monitorType == RowActionMonitor.MONITOR_EXPORT) {
          setRowInfo(0);
          setInfoText(ResourceMgr.getString("MsgSpoolingRow"));
          setObject(object + " [" + info + "]");
        } else {
          setInfoText(object);
          rowInfo.setText(info);
        }
      }
    });
  }

  @Override
  public void setCurrentRow(long currentRow, long totalRows) {
    if (currentRow > -1 && totalRows > -1) {
      this.rowInfo.setText(NumberStringCache.getNumberString(currentRow) + "/" + NumberStringCache.getNumberString(totalRows));
    }
    if (currentRow > -1) {
      this.rowInfo.setText(NumberStringCache.getNumberString(currentRow));
    } else {
      this.rowInfo.setText("");
    }
  }

  @Override
  public void saveCurrentType(String type) {
  }

  @Override
  public void restoreType(String type) {
  }

  @Override
  public int getMonitorType() {
    return monitorType;
  }

  @Override
  public void setMonitorType(int aType) {
    monitorType = aType;
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

    infoPanel = new JPanel();
    progressInfoText = new JTextField();
    rowInfo = new JLabel();
    cancelButton = new WbButton();
    fileNameField = new JTextField();

    setMinimumSize(new Dimension(250, 120));
    setPreferredSize(new Dimension(250, 120));
    setLayout(new GridBagLayout());

    infoPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
    infoPanel.setLayout(new BorderLayout(0, 5));

    progressInfoText.setEditable(false);
    progressInfoText.setBorder(null);
    progressInfoText.setDisabledTextColor(progressInfoText.getForeground());
    infoPanel.add(progressInfoText, BorderLayout.CENTER);

    rowInfo.setHorizontalAlignment(SwingConstants.RIGHT);
    rowInfo.setMinimumSize(new Dimension(30, 18));
    infoPanel.add(rowInfo, BorderLayout.EAST);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(5, 6, 0, 6);
    add(infoPanel, gridBagConstraints);

    cancelButton.setText(ResourceMgr.getString("LblCancel"));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        cancelButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.SOUTH;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(12, 0, 10, 0);
    add(cancelButton, gridBagConstraints);

    fileNameField.setEditable(false);
    fileNameField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(4, 6, 0, 6);
    add(fileNameField, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
  {//GEN-HEADEREND:event_cancelButtonActionPerformed
    if (this.task instanceof InterruptableJob) {
      String msg = ResourceMgr.getString("MsgCancelAllCurrent");
      String current = ResourceMgr.getString("LblCancelCurrentExport");
      String all = ResourceMgr.getString("LblCancelAllExports");
      int answer = WbSwingUtilities.getYesNo(parent, msg, new String[]{current, all});
      InterruptableJob job = (InterruptableJob) task;
      if (answer == JOptionPane.YES_OPTION) {
        job.cancelCurrent();
      } else {
        job.cancelExecution();
      }
    } else if (task != null) {
      task.cancelExecution();
    }
  }//GEN-LAST:event_cancelButtonActionPerformed
  // End of variables declaration//GEN-END:variables

}
