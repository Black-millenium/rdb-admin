/*
 * JLineWrapper.java
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
package workbench.console;

import jline.*;
import workbench.util.CollectionUtil;
import workbench.util.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Kellerer
 */
public class JLineWrapper
    implements WbConsole {
  private ConsoleReader reader;

  public JLineWrapper()
      throws IOException {
    reader = new ConsoleReader();
    reader.setUseHistory(true);
    reader.setUsePagination(false);
    reader.setBellEnabled(false);
    List<Completor> completors = new ArrayList<Completor>(2);
//		completors.add(new WbFilenameCompletor());
    completors.add(new ClipCompletor());
    completors.add(new NullCompletor());
    reader.addCompletor(new ArgumentCompletor(completors));
  }

  @Override
  public void clearScreen() {
    try {
      reader.clearScreen();
    } catch (IOException ex) {
    }
  }

  @Override
  public void reset() {
    try {
      reader.getInput().reset();
    } catch (IOException ex) {
    }
  }

  @Override
  public char readCharacter() {
    try {
      return (char) reader.readVirtualKey();
    } catch (IOException ex) {
    }
    return 0;
  }

  @Override
  public String readLineWithoutHistory(String prompt) {
    boolean old = reader.getUseHistory();
    try {
      reader.setUseHistory(false);
      return readLine(prompt);
    } finally {
      reader.setUseHistory(old);
    }
  }

  @Override
  public int getColumns() {
    Terminal t = Terminal.getTerminal();
    if (t != null) {
      return t.getTerminalWidth();
    }
    return -1;
  }

  @Override
  public String readPassword(String prompt) {
    try {
      return reader.readLine(prompt, Character.valueOf('*'));
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public String readLine(String prompt) {
    try {
      return reader.readLine(prompt);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public void shutdown() {
    History h = reader.getHistory();
    if (h != null) {
      FileUtil.closeQuietely(h.getOutput());
    }
  }

  @Override
  public void clearHistory() {
    History h = reader.getHistory();
    if (h != null) {
      h.clear();
    }
  }

  @Override
  public void addToHistory(List<String> lines) {
    if (CollectionUtil.isEmpty(lines)) return;

    History h = reader.getHistory();
    if (h != null) {
      for (String line : lines) {
        h.addToHistory(line);
      }
    }
  }


}
