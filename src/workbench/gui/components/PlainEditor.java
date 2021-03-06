/*
 * PlainEditor.java
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
import workbench.gui.editor.SearchAndReplace;
import workbench.interfaces.Restoreable;
import workbench.interfaces.TextContainer;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A simple text editor based on a JTextArea.
 * The panel displays also a checkbox to turn word wrapping on and off
 * and optionally an information label.
 *
 * @author Thomas Kellerer
 */
public class PlainEditor
    extends JPanel
    implements ActionListener, TextContainer, Restoreable {
  private JTextArea editor;
  private JCheckBox wordWrap;
  private Color enabledBackground;
  private JLabel infoText;
  private JPanel toolPanel;
  private JScrollPane scroll;

  public PlainEditor() {
    super();
    editor = new JTextArea();
    this.enabledBackground = editor.getBackground();
    editor.putClientProperty("JTextArea.infoBackground", Boolean.TRUE);
    TextComponentMouseListener l = new TextComponentMouseListener(this.editor);

    scroll = new JScrollPane(editor);
    editor.setLineWrap(true);
    editor.setWrapStyleWord(true);
    editor.setFont(Settings.getInstance().getEditorFont());
    this.setLayout(new BorderLayout());
    toolPanel = new JPanel();
    toolPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    wordWrap = new JCheckBox(ResourceMgr.getString("LblWordWrap"));
    wordWrap.setSelected(true);
    wordWrap.addActionListener(this);
    toolPanel.add(wordWrap);

    this.add(toolPanel, BorderLayout.NORTH);
    this.add(scroll, BorderLayout.CENTER);
    this.setFocusable(false);
    Document d = editor.getDocument();
    if (d != null) {
      int tabSize = Settings.getInstance().getEditorTabWidth();
      d.putProperty(PlainDocument.tabSizeAttribute, Integer.valueOf(tabSize));
    }
    SearchAndReplace replacer = new SearchAndReplace(this, this);
    l.addAction(replacer.getFindAction());
    l.addAction(replacer.getFindNextAction());
    l.addAction(replacer.getReplaceAction());
  }

  public void removeBorders() {
    scroll.setBorder(WbSwingUtilities.EMPTY_BORDER);
    editor.setBorder(WbSwingUtilities.EMPTY_BORDER);
    toolPanel.setBorder(DividerBorder.BOTTOM_DIVIDER);
  }

  @Override
  public int getCaretPosition() {
    return this.editor.getCaretPosition();
  }

  @Override
  public void setCaretPosition(int pos) {
    this.editor.setCaretPosition(pos);
  }

  @Override
  public int getSelectionEnd() {
    return this.editor.getSelectionEnd();
  }

  @Override
  public int getSelectionStart() {
    return this.editor.getSelectionStart();
  }

  @Override
  public void select(int start, int end) {
    this.editor.select(start, end);
  }

  public void setInfoText(String text) {
    if (this.infoText == null) {
      this.infoText = new JLabel();
      this.toolPanel.add(Box.createHorizontalStrut(10));
      this.toolPanel.add(infoText);
    }
    this.infoText.setText(text);
  }

  @Override
  public void requestFocus() {
    this.editor.requestFocus();
  }

  @Override
  public boolean requestFocusInWindow() {
    return this.editor.requestFocusInWindow();
  }

  @Override
  public void restoreSettings() {
    boolean wrap = Settings.getInstance().getPlainEditorWordWrap();
    wordWrap.setSelected(wrap);
    this.editor.setLineWrap(wrap);
  }

  @Override
  public void saveSettings() {
    Settings.getInstance().setPlainEditorWordWrap(wordWrap.isSelected());
  }

  @Override
  public String getText() {
    return this.editor.getText();
  }

  @Override
  public void setText(String aText) {
    this.editor.setText(aText);
  }

  @Override
  public String getSelectedText() {
    return this.editor.getSelectedText();
  }

  @Override
  public void setSelectedText(String aText) {
    this.editor.replaceSelection(aText);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    this.editor.setLineWrap(this.wordWrap.isSelected());
  }

  @Override
  public boolean isEditable() {
    return this.editor.isEditable();
  }

  @Override
  public void setEditable(boolean flag) {
    this.editor.setEditable(flag);
    this.editor.setBackground(enabledBackground);
  }

  @Override
  public boolean isTextSelected() {
    return (getSelectionStart() < getSelectionEnd());
  }

}
