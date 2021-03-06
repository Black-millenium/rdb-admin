/*
 * WbListPkDef.java
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

import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.storage.PkMapping;

import java.sql.SQLException;

/**
 * @author Thomas Kellerer
 */
public class WbListPkDef
    extends SqlCommand {

  public static final String VERB = "WbListPkDef";

  @Override
  public String getVerb() {
    return VERB;
  }

  @Override
  protected boolean isConnectionRequired() {
    return false;
  }

  @Override
  public StatementRunnerResult execute(String aSql)
      throws SQLException {
    StatementRunnerResult result = new StatementRunnerResult();

    result.setSuccess();

    String info = PkMapping.getInstance().getMappingAsText();
    if (info != null) {
      result.addMessage(ResourceMgr.getString("MsgPkDefinitions"));
      result.addMessage("");
      result.addMessage(info);
      result.addMessage(ResourceMgr.getString("MsgPkDefinitionsEnd"));
    } else {
      result.addMessage(ResourceMgr.getString("MsgPkDefinitionsEmpty"));
    }
    return result;
  }

  @Override
  public boolean isWbCommand() {
    return true;
  }
}
