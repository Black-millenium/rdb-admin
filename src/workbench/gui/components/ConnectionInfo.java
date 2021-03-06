/*
 * ConnectionInfo.java
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

import workbench.db.ConnectionProfile;
import workbench.db.WbConnection;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.WbAction;
import workbench.gui.tools.ConnectionInfoPanel;
import workbench.resource.IconMgr;
import workbench.resource.ResourceMgr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Thomas Kellerer
 */
public class ConnectionInfo
    extends JPanel
    implements PropertyChangeListener, ActionListener, MouseListener {
  private final Runnable updater;
  private WbConnection sourceConnection;
  private Color defaultBackground;
  private WbAction showInfoAction;
  private WbLabelField infoText;
  private JLabel iconLabel;
  private boolean useCachedSchema;

  public ConnectionInfo(Color aBackground) {
    super(new GridBagLayout());
    infoText = new WbLabelField();
    infoText.setOpaque(false);

    setOpaque(true);

    if (aBackground != null) {
      setBackground(aBackground);
      defaultBackground = aBackground;
    } else {
      defaultBackground = infoText.getBackground();
    }
    showInfoAction = new WbAction(this, "show-info");
    showInfoAction.setMenuTextByKey("MnuTxtConnInfo");
    showInfoAction.setEnabled(false);
    infoText.addPopupAction(showInfoAction);
    infoText.setText(ResourceMgr.getString("TxtNotConnected"));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.gridx = 1;
    c.gridy = 0;
    c.anchor = GridBagConstraints.WEST;
    add(infoText, c);
    updater = new Runnable() {
      @Override
      public void run() {
        _updateDisplay();
      }
    };
  }

  public void setConnection(WbConnection aConnection) {
    if (this.sourceConnection != null) {
      this.sourceConnection.removeChangeListener(this);
    }

    this.sourceConnection = aConnection;

    Color bkg = null;

    if (this.sourceConnection != null) {
      this.sourceConnection.addChangeListener(this);
      ConnectionProfile p = aConnection.getProfile();
      if (p != null) {
        bkg = p.getInfoDisplayColor();
      }
    }
    showInfoAction.setEnabled(this.sourceConnection != null);

    if (bkg == null) {
      setBackground(defaultBackground);
    } else {
      setBackground(bkg);
    }

    useCachedSchema = true;
    try {
      updateDisplay();
    } finally {
      useCachedSchema = false;
    }
  }

  private void updateDisplay() {
    WbSwingUtilities.invoke(updater);
  }

  private void _updateDisplay() {
    if (this.sourceConnection != null) {
      infoText.setText(this.sourceConnection.getDisplayString(useCachedSchema));
      StringBuilder tip = new StringBuilder(30);
      tip.append("<html>");
      tip.append(this.sourceConnection.getDatabaseProductName());
      tip.append(" ");
      tip.append(this.sourceConnection.getDatabaseVersion().toString());
      tip.append("<br>");
      tip.append(ResourceMgr.getFormattedString("TxtDrvVersion", this.sourceConnection.getDriverVersion()));
      tip.append("</html>");
      infoText.setToolTipText(tip.toString());
    } else {
      infoText.setText(ResourceMgr.getString("TxtNotConnected"));
      infoText.setToolTipText(null);
    }
    infoText.setBackground(this.getBackground());
    infoText.setCaretPosition(0);
    showMode();

    invalidate();
    validate();

    if (getParent() != null) {
      getParent().invalidate();
      // this seems to be the only way to resize the component
      // approriately after setting a new text when using the dreaded GTK+ look and feel
      getParent().validate();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getSource() == this.sourceConnection) {
      updateDisplay();
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (this.sourceConnection == null) return;
    // if (!WbSwingUtilities.isConnectionIdle(this, sourceConnection)) return;

    ConnectionInfoPanel.showConnectionInfo(sourceConnection);
  }

  private void showMode() {
    String tooltip = null;
    if (sourceConnection == null) {
      hideIcon();
    } else {
      ConnectionProfile profile = sourceConnection.getProfile();
      boolean readOnly = profile.isReadOnly();
      boolean sessionReadonly = sourceConnection.isSessionReadOnly();
      if (readOnly && !sessionReadonly) {
        // the profile is set to read only, but it was changed temporarily
        showIcon("unlocked");
        tooltip = ResourceMgr.getString("TxtConnReadOnlyOff");
      } else if (readOnly || sessionReadonly) {
        showIcon("lock");
        tooltip = ResourceMgr.getString("TxtConnReadOnly");
      } else {
        hideIcon();
      }
    }
    if (this.iconLabel != null) {
      this.iconLabel.setToolTipText(tooltip);
    }
    invalidate();
  }

  private void hideIcon() {
    if (iconLabel != null) {
      iconLabel.removeMouseListener(this);
      remove(iconLabel);
      iconLabel = null;
    }
  }

  private void showIcon(String name) {
    if (iconLabel == null) {
      iconLabel = new JLabel();
      iconLabel.setOpaque(false);
      iconLabel.addMouseListener(this);
      iconLabel.setBackground(getBackground());
    }
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(0, 4, 0, 0);
    ImageIcon png = IconMgr.getInstance().getPngIcon(name, IconMgr.getInstance().getToolbarIconSize());
    iconLabel.setIcon(png);
    add(iconLabel, c);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 && sourceConnection != null) {
      ConnectionProfile profile = sourceConnection.getProfile();
      boolean profileReadOnly = profile.isReadOnly();
      boolean sessionReadOnly = sourceConnection.isSessionReadOnly();
      if (!sessionReadOnly && profileReadOnly) {
        sourceConnection.resetSessionReadOnly();
      }
      if (profileReadOnly && sessionReadOnly) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        boolean makeRead = WbSwingUtilities.getYesNo(parent, ResourceMgr.getString("MsgDisableReadOnly"));
        if (makeRead) {
          sourceConnection.setSessionReadOnly(false);
        }
      }
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  public void dispose() {
    if (showInfoAction != null) {
      showInfoAction.dispose();
    }
    infoText.dispose();
  }
}
