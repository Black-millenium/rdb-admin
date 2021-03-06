/*
 * FeedbackWindow.java
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
import workbench.util.StringUtil;
import workbench.util.WbThread;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Thomas Kellerer
 */
public class FeedbackWindow
    extends JDialog {
  private JLabel connectLabel;

  public FeedbackWindow(Frame owner, String msg) {
    super(owner, false);
    initComponents(msg);
  }

  public FeedbackWindow(Dialog owner, String msg) {
    super(owner, true);
    initComponents(msg);
  }

  private void initComponents(String msg) {
    JPanel p = new JPanel();
    p.setBorder(new CompoundBorder(WbSwingUtilities.getBevelBorderRaised(), new EmptyBorder(15, 20, 15, 20)));
    p.setLayout(new BorderLayout(0, 0));
    p.setMinimumSize(new Dimension(350, 50));
    connectLabel = new JLabel(msg);
    connectLabel.setMinimumSize(new Dimension(300, 50));
    connectLabel.setHorizontalAlignment(SwingConstants.CENTER);
    p.add(connectLabel, BorderLayout.CENTER);
    setUndecorated(true);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(p, BorderLayout.CENTER);
    pack();
  }

  public void showAndStart(final Runnable task) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        WbThread t = new WbThread(task, "FeedbackWindow");
        t.start();
        setVisible(true);
      }
    });
  }

  public String getMessage() {
    return connectLabel.getText();
  }

  public void setMessage(String msg) {
    if (StringUtil.isBlank(msg)) {
      connectLabel.setText("");
    } else {
      connectLabel.setText(msg);
    }
    pack();
  }

  public void forceRepaint() {
    WbSwingUtilities.invoke(new Runnable() {
      @Override
      public void run() {
        doLayout();
        invalidate();
        validate();
        repaint();
      }
    });
  }

}
