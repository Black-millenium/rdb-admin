/*
 * ComboStringPropertyEditor.java
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

import workbench.interfaces.SimplePropertyEditor;
import workbench.log.LogMgr;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.lang.reflect.Method;


/**
 * A property editor for a String property, where the values
 * for the field can also be selected by a dropdown.
 *
 * @author Thomas Kellerer
 */
public class ComboStringPropertyEditor
    extends JComboBox
    implements ItemListener, SimplePropertyEditor, FocusListener, DocumentListener {
  protected boolean changed;
  private Object source;
  private Method setter;
  private Method getter;
  private boolean immediateUpdate;
  private ActionListener listener;

  @Override
  public void setSourceObject(Object aSource, String aProperty) {
    this.source = aSource;
    this.changed = false;
    String propertyName = Character.toUpperCase(aProperty.charAt(0)) + aProperty.substring(1);
    stopEvents();

    try {
      String name = "get" + propertyName;
      Class cls = aSource.getClass();
      this.getter = cls.getMethod(name, (Class[]) null);

      name = "set" + propertyName;
      Class[] parms = {String.class};

      this.setter = cls.getMethod(name, parms);
      if (this.getModel() != null) {
        this.initData();
      }
    } catch (Exception e) {
      LogMgr.logError("ComboStringPropertyEditor.setSourceObject()", "Error during init", e);
    }
    startEvents();
  }

  private ActionListener getListener() {
    if (listener == null) {
      listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
          changed = true;
          applyChanges();
        }
      };
    }
    return listener;
  }

  private void stopEvents() {
    this.removeItemListener(this);
    if (this.isEditable()) {
      removeActionListener(getListener());
      JTextField text = (JTextField) getEditor().getEditorComponent();
      //text.removeFocusListener(this);
      text.getDocument().removeDocumentListener(this);
    }
  }

  private void startEvents() {
    this.addItemListener(this);
    if (this.isEditable()) {
      addActionListener(getListener());
      JTextField text = (JTextField) getEditor().getEditorComponent();
      //text.addFocusListener(this);
      text.getDocument().addDocumentListener(this);
    }
  }

  @Override
  public void setModel(ComboBoxModel m) {
    stopEvents();
    super.setModel(m);
    if (this.isEditable()) {
      this.initData();
    }
    startEvents();
  }

  private void initData() {
    if (this.getter == null || this.source == null) return;
    try {

      Object value = this.getter.invoke(this.source, (Object[]) null);
      this.setSelectedItem(value);
    } catch (Exception e) {
      LogMgr.logError("ComboStringPropertyEditor.intiData", "Error", e);
    }
  }

  @Override
  public boolean isChanged() {
    return this.changed;
  }

  @Override
  public void applyChanges() {
    if (!this.changed) return;
    Object[] args = new Object[1];
    if (this.isEditable()) {
      args[0] = this.getEditor().getItem().toString();
    } else {
      args[0] = this.getSelectedItem().toString();
    }
    try {
      this.setter.invoke(this.source, args);
    } catch (Exception e) {
      LogMgr.logError("ComboStringPropertyEditor.setSourceObject()", "Error during init", e);
    }
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      this.changed = true;
    }
    if (this.immediateUpdate) {
      this.applyChanges();
    }
  }

  @Override
  public boolean getImmediateUpdate() {
    return this.immediateUpdate;
  }

  @Override
  public void setImmediateUpdate(boolean aFlag) {
    this.immediateUpdate = aFlag;
    if (aFlag) this.applyChanges();
  }

  @Override
  public void focusGained(FocusEvent e) {
  }

  @Override
  public void focusLost(FocusEvent e) {
    this.applyChanges();
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    documentChanged();
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    documentChanged();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    documentChanged();
  }

  private void documentChanged() {
    this.changed = true;
    this.applyChanges();
  }
}

