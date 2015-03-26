/*
 * WbTabbedPane.java
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
import workbench.gui.lnf.LnFHelper;
import workbench.interfaces.Moveable;
import workbench.log.LogMgr;
import workbench.resource.GuiSettings;
import workbench.resource.Settings;
import workbench.util.MacOSHelper;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A JTabbedPane that allows re-ordering of the tabs using drag & drop.
 * <br/>
 * Additionally it installs its own UI to remove the unnecessary borders
 * that the standard Java Look & Feels create.
 * <br/>
 * A close button can be displayed inside each tab that will trigger
 * an event with the registered TabCloser.
 *
 * @author Thomas Kellerer
 */
public class WbTabbedPane
    extends JTabbedPane
    implements MouseListener, MouseMotionListener, ChangeListener, PropertyChangeListener {
  private Moveable tabMover;
  private int draggedTabIndex;
  private TabCloser tabCloser;
  private boolean hideDisabledButtons;
  private boolean alwaysUseCustomComponent;
  private boolean onlyCloseActive;
  private Point dragStart;
  private Rectangle tabBounds;
  private int previousTabIndex;

  public WbTabbedPane() {
    super();
    init();
  }

  public WbTabbedPane(int placement) {
    super(placement);
    init();
  }

  public void hideDisabledButtons(boolean flag) {
    hideDisabledButtons = flag;
  }

  public void setCloseButtonEnabled(Component panel, boolean flag) {
    if (tabCloser == null) return;

    int index = indexOfComponent(panel);
    if (index == -1) return;
    setCloseButtonEnabled(index, flag);
  }

  public void setCloseButtonEnabled(int index, boolean flag) {
    if (tabCloser == null) return;

    TabButtonComponent comp = getTabButton(index);
    if (comp != null) {
      comp.setEnabled(flag);
    }
  }

  protected TabButtonComponent getTabButton(int index) {
    TabButtonComponent comp = (TabButtonComponent) getTabComponentAt(index);
    return comp;
  }

  @Override
  public void setDisplayedMnemonicIndexAt(int tabIndex, int mnemonicIndex) {
    super.setDisplayedMnemonicIndexAt(tabIndex, mnemonicIndex);
    TabButtonComponent comp = getTabButton(tabIndex);
    if (comp != null) {
      comp.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  @Override
  public void setMnemonicAt(int tabIndex, int mnemonic) {
    super.setMnemonicAt(tabIndex, mnemonic);
    TabButtonComponent comp = getTabButton(tabIndex);
    if (comp != null) {
      comp.setDisplayedMnemonic(mnemonic);
    }
  }

  @Override
  public void setIconAt(int index, Icon icon) {
    super.setIconAt(index, icon);
    TabButtonComponent comp = getTabButton(index);
    if (comp != null) {
      comp.setIcon(icon);
    }
  }

  /**
   * Enable/Disable the close button.
   *
   * @param closer the TabCloser to handle the close event. If null the close button will be hidden
   */
  public void showCloseButton(TabCloser closer) {
    boolean wasAdded = (closer != null && tabCloser == null);
    boolean wasRemoved = (closer == null && tabCloser != null);

    tabCloser = closer;
    if (wasAdded) {
      addCloseButtons();
      updateButtons();
      addChangeListener(this);
    } else if (wasRemoved) {
      removeCloseButtons();
      removeChangeListener(this);
    }
  }

  protected void addCloseButtons() {
    if (tabCloser == null) return;

    for (int i = 0; i < getTabCount(); i++) {
      String title = getTitleAt(i);
      Icon icon = getIconAt(i);
      TabButtonComponent comp = new TabButtonComponent(title, this, true);
      comp.setIcon(icon);
      setTabComponentAt(i, comp);
    }
  }

  protected void removeCloseButtons() {
    for (int i = 0; i < getTabCount(); i++) {
      if (alwaysUseCustomComponent) {
        TabButtonComponent comp = getTabButton(i);
        if (comp != null) {
          comp.setButtonVisible(false);
        }
      } else {
        setTabComponentAt(i, null);
      }
    }
  }

  public void closeButtonClicked(final int index) {
    if (tabCloser == null) return;
    if (!tabCloser.canCloseTab(index)) return;

    if (onlyCloseActive && index != getSelectedIndex()) {
      EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          setSelectedIndex(index);
        }
      });

    } else {
      EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          tabCloser.tabCloseButtonClicked(index);
        }
      });
    }
  }

  public int getTabHeight() {
    Font font = getFont();
    if (font == null) {
      return 0;
    }
    FontMetrics metrics = getFontMetrics(font);
    if (metrics == null) {
      return 0;
    }
    int fontHeight = metrics.getHeight();
    Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
    if (tabInsets != null) {
      fontHeight += tabInsets.top + tabInsets.bottom + 2;
    }
    return fontHeight + 5;
  }

  @Override
  public JToolTip createToolTip() {
    JToolTip tip = new MultiLineToolTip();
    tip.setComponent(this);
    return tip;
  }

  private void init() {
    // For use with the jGoodies Plastic look & feel
    putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);
    putClientProperty("jgoodies.embeddedTabs", Boolean.valueOf(System.getProperty("jgoodies.embeddedTabs", "false")));
    alwaysUseCustomComponent = !LnFHelper.isJGoodies() && !MacOSHelper.isMacOS();
    try {
      TabbedPaneUI tui = TabbedPaneUIFactory.getBorderLessUI();
      if (tui != null) {
        this.setUI(tui);
      }
    } catch (Exception e) {
      LogMgr.logError("WbTabbedPane.init()", "Error during init", e);
    }
    onlyCloseActive = GuiSettings.getCloseActiveTabOnly();
    Settings.getInstance().addPropertyChangeListener(this, GuiSettings.PROPERTY_CLOSE_ACTIVE_TAB);
    addChangeListener(this);
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    Settings.getInstance().removePropertyChangeListener(this);
  }

  @Override
  public Insets getInsets() {
    return WbSwingUtilities.EMPTY_INSETS;
  }

  @Override
  public void setTitleAt(int index, String title) {
    super.setTitleAt(index, title);
    TabButtonComponent comp = getTabButton(index);
    if (comp != null) {
      comp.setTitle(title);
    }
  }

  @Override
  public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    super.insertTab(title, icon, component, tip, index);
    if (alwaysUseCustomComponent || tabCloser != null) {
      // Always insert our own tab component to work around a bug with HTML rendering
      // in newer JDKs, see: http://bugs.sun.com/view_bug.do?bug_id=6670274
      setTabComponentAt(index, new TabButtonComponent(title, this, tabCloser != null));
    }
    if (tabCloser != null) {
      updateButtons();
    }
  }

  /**
   * The empty override is intended, to give public access to the method
   */
  @Override
  public void fireStateChanged() {
    super.fireStateChanged();
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean isManagingFocus() {
    return false;
  }

  @Override
  public boolean isRequestFocusEnabled() {
    return false;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean isFocusTraversable() {
    return false;
  }

  @Override
  public boolean isFocusable() {
    return false;
  }

  public void disableDragDropReordering() {
    this.removeMouseListener(this);
    this.removeMouseMotionListener(this);
    this.tabMover = null;
    draggedTabIndex = -1;
  }

  public void enableDragDropReordering(Moveable mover) {
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.tabMover = mover;
    draggedTabIndex = -1;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    int index = getUI().tabForCoordinate(this, e.getX(), e.getY());
    if (index < 0) {
      dragStart = null;
      draggedTabIndex = -1;
      tabBounds = null;
      return;
    }

    dragStart = e.getPoint();

    tabBounds = getUI().getTabBounds(this, index);

    if (this.tabMover != null) {
      if (this.tabMover.startMove(index)) {
        draggedTabIndex = index;
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    Point dragEnd = e.getPoint();
    if (dragStart == null) return;

    // Check if the mouse was moved at all to avoid unnecessary events
    double distance = Math.abs(dragStart.distance(dragEnd));
    boolean inside = tabBounds.contains(dragEnd);

    if (this.tabMover != null && distance > 2 && inside) {
      this.tabMover.endMove(draggedTabIndex);
    } else {
      this.tabMover.moveCancelled();
    }
    draggedTabIndex = -1;
    dragStart = null;
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (tabMover == null) return;
    if (draggedTabIndex == -1) return;

    int newIndex = getUI().tabForCoordinate(this, e.getX(), e.getY());

    if (newIndex != -1 && newIndex != draggedTabIndex) {
      setCursor(DragSource.DefaultMoveDrop);
      if (tabMover.moveTab(draggedTabIndex, newIndex)) {
        draggedTabIndex = newIndex;
      }
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
  }

  private void updateButtons() {
    if (tabCloser == null) return;

    int count = getTabCount();
    int index = getSelectedIndex();

    for (int i = 0; i < count; i++) {
      boolean canClose = tabCloser.canCloseTab(i);
      setCloseButtonEnabled(i, canClose);
      TabButtonComponent tab = getTabButton(i);
      if (tab != null) {
        if (hideDisabledButtons) {
          tab.setButtonVisible(canClose);
        }
        if (onlyCloseActive) {
          tab.setRolloverEnabled(i == index);
        } else {
          tab.setRolloverEnabled(true);
        }
      }
    }
  }

  public int getPreviousTabIndex() {
    return previousTabIndex;
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    updateButtons();
  }

  @Override
  public void setSelectedIndex(int index) {
    previousTabIndex = getSelectedIndex();
    super.setSelectedIndex(index);
  }

  @Override
  public void removeTabAt(int index) {
    super.removeTabAt(index);
    previousTabIndex = -1;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    onlyCloseActive = GuiSettings.getCloseActiveTabOnly();
    updateButtons();
  }
}
