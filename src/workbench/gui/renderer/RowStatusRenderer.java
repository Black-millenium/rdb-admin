/*
 * RowStatusRenderer.java
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
package workbench.gui.renderer;

import workbench.resource.IconMgr;
import workbench.resource.ResourceMgr;
import workbench.storage.RowData;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * A renderer to display the status of a row in the result set.
 * <br/>
 * It displays different icons for new and modified, depending on the status of the row.
 *
 * @author Thomas Kellerer
 */
public class RowStatusRenderer
    extends DefaultTableCellRenderer {
  private final ImageIcon modifiedIcon = IconMgr.getInstance().getLabelIcon("modifiedrow");
  private final ImageIcon newIcon = IconMgr.getInstance().getLabelIcon("newrow");

  private final String newTip = ResourceMgr.getString("TxtRowNew");
  private final String modifiedTip = ResourceMgr.getString("TxtRowModified");
  private final String notModifiedTip = ResourceMgr.getString("TxtRowNotModified");

  public RowStatusRenderer() {
    super();
    Dimension dim = new Dimension(modifiedIcon.getIconWidth() + 2, newIcon.getIconHeight() + 2);
    this.setMaximumSize(dim);
    this.setMinimumSize(dim);
    this.setPreferredSize(dim);
    this.setText(null);
    this.setIconTextGap(0);
    this.setHorizontalAlignment(JLabel.LEFT);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    try {
      int status = (value == null ? RowData.NOT_MODIFIED : ((Integer) value).intValue());
      switch (status) {
        case RowData.NEW:
          this.setIcon(newIcon);
          this.setToolTipText(newTip);
          break;
        case RowData.MODIFIED:
          this.setIcon(modifiedIcon);
          this.setToolTipText(modifiedTip);
          break;
        default:
          this.setIcon(null);
          this.setToolTipText(notModifiedTip);
      }
    } catch (Exception e) {
      this.setIcon(null);
      this.setToolTipText(notModifiedTip);
    }
    return this;
  }

}
