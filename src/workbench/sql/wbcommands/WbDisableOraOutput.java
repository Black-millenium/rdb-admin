/*
 * WbDisableOraOutput.java
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

import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.util.ArgumentParser;
import workbench.util.ArgumentType;

import java.sql.SQLException;

import static workbench.sql.wbcommands.WbEnableOraOutput.PARAM_QUIET;

/**
 * @author Thomas Kellerer
 */
public class WbDisableOraOutput extends SqlCommand {
  public static final String VERB = "DISABLEOUT";

  public WbDisableOraOutput() {
    super();
    this.cmdLine = new ArgumentParser(false);
    this.cmdLine.addArgument(WbEnableOraOutput.PARAM_QUIET, ArgumentType.BoolSwitch);
  }

  @Override
  public String getVerb() {
    return VERB;
  }

  @Override
  public StatementRunnerResult execute(String sql)
      throws SQLException {
    StatementRunnerResult result = new StatementRunnerResult();
    currentConnection.getMetadata().disableOutput();
    cmdLine.parse(getCommandLine(sql));
    if (cmdLine.getBoolean(PARAM_QUIET)) {
      LogMgr.logDebug("WbEnableOraOutput.execute()", "Support for dbms_output disabled");
    } else {
      result.addMessage(ResourceMgr.getString("MsgDbmsOutputDisabled"));
    }
    return result;
  }

  @Override
  public boolean isWbCommand() {
    return true;
  }

}
