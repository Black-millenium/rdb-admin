/*
 * ObjectScripterUI.java
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

import workbench.db.WbConnection;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.CreateSnippetAction;
import workbench.gui.components.RunningJobIndicator;
import workbench.gui.components.WbStatusLabel;
import workbench.gui.sql.EditorPanel;
import workbench.interfaces.ScriptGenerationMonitor;
import workbench.interfaces.Scripter;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;
import workbench.util.WbThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowListener;

/**
 * @author Thomas Kellerer
 */
public class ObjectScripterUI
    extends JPanel
    implements WindowListener, ScriptGenerationMonitor {
  private final Object runMonitor = new Object();
  protected Scripter scripter;
  protected JLabel statusMessage;
  protected EditorPanel editor;
  protected JFrame window;
  private boolean isRunning;

  public ObjectScripterUI(Scripter script) {
    super();
    this.scripter = script;
    this.scripter.setProgressMonitor(this);

    this.statusMessage = new WbStatusLabel();
    this.setLayout(new BorderLayout());
    this.add(this.statusMessage, BorderLayout.SOUTH);
    this.editor = EditorPanel.createSqlEditor();
    CreateSnippetAction create = new CreateSnippetAction(this.editor);
    this.editor.addPopupMenuItem(create, true);
    this.add(this.editor, BorderLayout.CENTER);
  }

  public void setDbConnection(WbConnection con) {
    editor.setDatabaseConnection(con);
  }

  private boolean isRunning() {
    synchronized (runMonitor) {
      return this.isRunning;
    }
  }

  private void setRunning(boolean flag) {
    synchronized (runMonitor) {
      this.isRunning = flag;
    }
  }

  private void startScripting() {
    if (this.isRunning()) return;

    if (!WbSwingUtilities.isConnectionIdle(this, scripter.getCurrentConnection())) {
      return;
    }

    WbThread t = new WbThread("ObjectScripter Thread") {
      @Override
      public void run() {
        String baseTitle = window.getTitle();
        try {
          setRunning(true);
          window.setTitle(RunningJobIndicator.TITLE_PREFIX + baseTitle);
          scripter.generateScript();
          if (!scripter.isCancelled()) {
            WbSwingUtilities.invoke(new Runnable() {
              @Override
              public void run() {
                editor.setText(scripter.getScript().toString());
                editor.setCaretPosition(0);
              }
            });
          }
        } finally {
          window.setTitle(baseTitle);
          setRunning(false);
          EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
              statusMessage.setText(StringUtil.EMPTY_STRING);
            }
          });
        }
      }
    };
    t.start();
  }

  @Override
  public void setCurrentObject(String aTableName, int current, int total) {
    if (current > 0 && total > 0) {
      this.statusMessage.setText(aTableName + " (" + current + "/" + total + ")");
    } else {
      this.statusMessage.setText(aTableName);
    }
    this.statusMessage.repaint();
  }

  public void show(Window aParent) {
    if (this.window == null) {
      this.window = new JFrame(ResourceMgr.getString("TxtWindowTitleGeneratedScript"));
      this.window.getContentPane().setLayout(new BorderLayout());
      this.window.getContentPane().add(this, BorderLayout.CENTER);
      ResourceMgr.setWindowIcons(window, "script");
      if (!Settings.getInstance().restoreWindowSize(this.window, ObjectScripterUI.class.getName())) {
        this.window.setSize(500, 400);
      }

      if (!Settings.getInstance().restoreWindowPosition(this.window, ObjectScripterUI.class.getName())) {
        WbSwingUtilities.center(this.window, aParent);
      }
      this.window.addWindowListener(this);
    }
    this.window.setVisible(true);
    this.startScripting();
  }

  @Override
  public void windowActivated(java.awt.event.WindowEvent e) {
  }

  @Override
  public void windowClosed(java.awt.event.WindowEvent e) {
  }

  private void cancel() {
    WbThread t = new WbThread("Scripter Cancel") {
      @Override
      public void run() {
        try {
          WbSwingUtilities.showWaitCursor(window);
          statusMessage.setText(ResourceMgr.getString("MsgCancelling"));
          Thread.yield();
          scripter.cancel();
        } catch (Throwable ex) {
          ex.printStackTrace();
        } finally {
          WbSwingUtilities.showDefaultCursor(window);
        }
        scripter = null;
        setRunning(false);
        closeWindow();
      }
    };
    t.start();
  }

  protected void closeWindow() {
    if (isRunning()) return;
    Settings.getInstance().storeWindowPosition(this.window, ObjectScripterUI.class.getName());
    Settings.getInstance().storeWindowSize(this.window, ObjectScripterUI.class.getName());
    this.window.setVisible(false);
    this.window.dispose();
  }

  @Override
  public void windowClosing(java.awt.event.WindowEvent e) {
    if (this.isRunning()) {
      cancel();
      return;
    }
    closeWindow();
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

}
