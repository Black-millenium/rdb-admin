/*
 * NoSelectionModel.java
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

import javax.swing.*;
import javax.swing.event.ListSelectionListener;

/**
 * @author Thomas Kellerer
 */
public class NoSelectionModel
    implements ListSelectionModel {

  @Override
  public void addListSelectionListener(ListSelectionListener x) {
  }

  @Override
  public void addSelectionInterval(int index0, int index1) {
  }

  @Override
  public void clearSelection() {
  }

  @Override
  public int getAnchorSelectionIndex() {
    return -1;
  }

  @Override
  public void setAnchorSelectionIndex(int index) {
  }

  @Override
  public int getLeadSelectionIndex() {
    return -1;
  }

  @Override
  public void setLeadSelectionIndex(int index) {
  }

  /**
   * Returns the last selected index or -1 if the selection is empty.
   */
  @Override
  public int getMaxSelectionIndex() {
    return -1;
  }

  @Override
  public int getMinSelectionIndex() {
    return -1;
  }

  @Override
  public int getSelectionMode() {
    return 0;
  }

  @Override
  public void setSelectionMode(int selectionMode) {
  }

  @Override
  public boolean getValueIsAdjusting() {
    return false;
  }

  @Override
  public void setValueIsAdjusting(boolean valueIsAdjusting) {
  }

  @Override
  public void insertIndexInterval(int index, int length, boolean before) {
  }

  @Override
  public boolean isSelectedIndex(int index) {
    return false;
  }

  @Override
  public boolean isSelectionEmpty() {
    return true;
  }

  @Override
  public void removeIndexInterval(int index0, int index1) {
  }

  @Override
  public void removeListSelectionListener(ListSelectionListener x) {
  }

  @Override
  public void removeSelectionInterval(int index0, int index1) {
  }

  @Override
  public void setSelectionInterval(int index0, int index1) {
  }

}
