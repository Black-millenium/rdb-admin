/*
 * ClearCompletionCacheAction.java
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
package workbench.gui.actions;

import workbench.db.WbConnection;
import workbench.db.objectcache.DbObjectCache;
import workbench.resource.ResourceMgr;

import java.awt.event.ActionEvent;

/**
 * Action to clear the cache for code completion
 *
 * @author Thomas Kellerer
 * @see DbObjectCache
 */
public class ClearCompletionCacheAction
    extends WbAction {
  private WbConnection dbConnection;

  public ClearCompletionCacheAction() {
    super();
    this.initMenuDefinition("MnuTxtClearCompletionCache");
    this.setMenuItemName(ResourceMgr.MNU_TXT_SQL);
    this.setEnabled(false);
  }

  public void setConnection(WbConnection conn) {
    this.dbConnection = conn;
    this.setEnabled(this.dbConnection != null);
  }

  @Override
  public void executeAction(ActionEvent e) {
    if (this.dbConnection != null) {
      this.dbConnection.getObjectCache().removeAll();
    }
  }
}
