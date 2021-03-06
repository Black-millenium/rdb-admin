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
package workbench.gui.tools;

import workbench.db.ColumnIdentifier;
import workbench.resource.ResourceMgr;

/**
 * @author Thomas Kellerer
 */

class ColumnMapRow {
  private ColumnIdentifier source;
  private ColumnIdentifier target;

  ColumnIdentifier getSource() {
    return this.source;
  }

  void setSource(ColumnIdentifier o) {
    this.source = o;
  }

  ColumnIdentifier getTarget() {
    return this.target;
  }

  void setTarget(ColumnIdentifier id) {
    this.target = id;
  }

  @Override
  public String toString() {
    return "Mapping " + source + " -> " + target;
  }
}

class SkipColumnIndicator {
  private final String display = ResourceMgr.getString("LblDPDoNotCopyColumns");

  @Override
  public String toString() {
    return display;
  }

}
