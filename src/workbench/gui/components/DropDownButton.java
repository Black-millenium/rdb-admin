/*
 * DropDownButton.java
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

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Thomas Kellerer
 */
public class DropDownButton
    extends WbButton
    implements ActionListener, PopupMenuListener {
  private JPopupMenu popup;
  private boolean popupVisible;
  private Border menuBorder = BorderFactory.createLineBorder(Color.GRAY, 1);

  public DropDownButton(String label) {
    super(label);
    init();
  }

  public DropDownButton(Icon i) {
    super(i);
    init();
  }

  private void init() {
    setFocusable(false);
    addActionListener(this);
    setMargin(WbSwingUtilities.EMPTY_INSETS);
    setBorderPainted(false);
    enableToolbarRollover();
  }

  public void dispose() {
    if (this.popup != null) {
      this.popup.removePopupMenuListener(this);
      this.popup.setVisible(false);
      this.popup.removeAll();
      this.popup = null;
    }
  }

  public void setDropDownMenu(JPopupMenu menu) {
    if (this.popup != null) {
      this.popup.removePopupMenuListener(this);
      this.popup.setVisible(false);
      this.popup.removeAll();
    }
    popup = menu;
    popup.setBorder(this.menuBorder);
    popup.addPopupMenuListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    if (this.popup == null) return;

    if (popupVisible) {
      popup.setVisible(false);
      popupVisible = false;
    } else {
      popup.show(this, 0, getHeight() - 2);
      popupVisible = true;
    }
  }

  @Override
  public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    popupVisible = true;
  }

  @Override
  public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    popupVisible = false;
  }

  @Override
  public void popupMenuCanceled(PopupMenuEvent e) {
    popupVisible = false;
  }
}
