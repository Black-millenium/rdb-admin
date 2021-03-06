/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015 Thomas Kellerer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.gui.components;

import workbench.util.MacOSHelper;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Thomas Kellerer
 */
public class ComboboxEditorWrapper
    implements ComboBoxEditor {
  private final ComboBoxEditor delegate;

  public ComboboxEditorWrapper(ComboBoxEditor editor) {
    this.delegate = editor;
    if (MacOSHelper.isMacOS()) {
      // inspired by: http://stackoverflow.com/questions/1543480/mac-lf-problems-differing-behavior-of-jtextfield-requestfocus
      Component comp = editor.getEditorComponent();
      if (comp instanceof JTextField) {
        JTextField text = (JTextField) comp;
        text.putClientProperty("Quaqua.TextComponent.autoSelect", Boolean.FALSE);
        text.putClientProperty("Quaqua.TextField.autoSelect", Boolean.FALSE);
        text.putClientProperty("TextComponent.autoSelect", Boolean.FALSE);
        text.putClientProperty("TextField.autoSelect=false", Boolean.FALSE);
        text.setCaret(new DefaultCaret());
      }
    }
  }

  @Override
  public Component getEditorComponent() {
    return delegate.getEditorComponent();
  }

  @Override
  public Object getItem() {
    return delegate.getItem();
  }

  @Override
  public void setItem(Object anObject) {
    String text = anObject == null ? "" : anObject.toString();
    JTextField editor = (JTextField) delegate.getEditorComponent();
    if (!editor.getText().equals(text)) {
      // only call setText() but do not call selectAll()
      editor.setText(text);
    }
  }

  @Override
  public void selectAll() {
    // don't do anything.
    // this is to avoid any implicit (and unwanted) selectAll() calls
  }

  @Override
  public void addActionListener(ActionListener l) {
    delegate.addActionListener(l);
  }

  @Override
  public void removeActionListener(ActionListener l) {
    delegate.removeActionListener(l);
  }

}
