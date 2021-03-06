/*
 * WbConnInfo.java
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
package workbench.sql.wbcommands;

import workbench.WbManager;
import workbench.db.ConnectionInfoBuilder;
import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;

import java.sql.SQLException;

/**
 * @author Thomas Kellerer
 */
public class WbConnInfo
    extends SqlCommand {
  public static final String VERB = "WbConnInfo";

  public WbConnInfo() {
  }

  @Override
  protected boolean isConnectionRequired() {
    return false;
  }

  @Override
  public StatementRunnerResult execute(String sql)
      throws SQLException, Exception {
    StatementRunnerResult result = new StatementRunnerResult(sql);
    result.setSuccess();

    if (this.currentConnection == null) {
      result.addMessage(ResourceMgr.getString("TxtNotConnected"));
    } else {
      try {
        currentConnection.setBusy(false);
        int indent = 0;
        ConnectionInfoBuilder info = new ConnectionInfoBuilder();
        if (WbManager.getInstance().isConsoleMode()) {
          result.addMessage(" ");
          indent = 2;
        }
        result.addMessage(info.getPlainTextDisplay(currentConnection, indent));
        if (WbManager.getInstance().isConsoleMode()) {
          result.addMessage("");
        }
      } finally {
        currentConnection.setBusy(true);
      }
    }
    return result;
  }

  @Override
  public String getVerb() {
    return VERB;
  }

  @Override
  public boolean isWbCommand() {
    return true;
  }

}
