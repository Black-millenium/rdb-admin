/*
 * VetoableListSelectionModel.java
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
package workbench.gui.profiles;

import workbench.interfaces.ListSelectionControl;

import javax.swing.*;

/**
 * A list selection model that supports "cancelling" of a selection change.
 * <p/>
 * The selection mode for this model is always SINGLE_SELECTION.
 *
 * @author Thomas Kellerer
 */
public class VetoableListSelectionModel
    extends DefaultListSelectionModel {
  private ListSelectionControl selectionController;

  public VetoableListSelectionModel(ListSelectionControl listControl) {
    super();
    if (listControl == null) {
      throw new NullPointerException("ListControl must not be null");
    }

    super.setSelectionMode(SINGLE_SELECTION);
    this.selectionController = listControl;
  }

  /**
   * Set the new selection but ask the registered ListSelectionControl before doing so.
   *
   * @param newIndex the index to be selected
   * @param ignored  not used because of the single selection mode
   * @see ListSelectionControl#canChangeSelection()
   */
  @Override
  public void setSelectionInterval(int newIndex, int ignored) {
    if (selectionController.canChangeSelection()) {
      super.setSelectionInterval(newIndex, newIndex);
    }
  }

  /**
   * Set the selection mode to SINGLE_SELECTION.
   *
   * @param selectionMode must be SINGLE_SELECTIONI
   * @throws IllegalArgumentException if selectionMode is not SINGLE_SELECTION
   */
  @Override
  public void setSelectionMode(int selectionMode) {
    if (selectionMode == SINGLE_SELECTION) {
      super.setSelectionMode(selectionMode);
    } else {
      throw new IllegalArgumentException("Only SINGLE_SELECTION_MODE allowed");
    }
  }

  @Override
  public void setLeadSelectionIndex(int leadIndex) {
    if (selectionController.canChangeSelection()) {
      super.setLeadSelectionIndex(leadIndex);
    }
  }

}
