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
package workbench.sql.wbcommands;

import workbench.db.ConnectionMgr;
import workbench.db.ConnectionProfile;
import workbench.db.WbConnection;
import workbench.gui.profiles.ProfileKey;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.sql.StatementRunnerResult;
import workbench.util.ArgumentParser;
import workbench.util.ExceptionUtil;
import workbench.util.StringUtil;

/**
 * @author Thomas Kellerer
 */
public class CommandLineConnectionHandler {
  private ArgumentParser cmdLine;
  private String profileNameArgument;
  private String profileGroupArgument;
  private String connectionArgument;

  public CommandLineConnectionHandler(ArgumentParser cmdLine, String profileNameArgument, String profileGroupArgument, String connectionArgument) {
    this.cmdLine = cmdLine;
    this.profileNameArgument = profileNameArgument;
    this.profileGroupArgument = profileGroupArgument;
    this.connectionArgument = connectionArgument;
  }

  public WbConnection getConnection(StatementRunnerResult result, WbConnection currentConnection, String baseDir, String id) {
    String desc = cmdLine.getValue(connectionArgument, null);
    if (StringUtil.isNonBlank(desc)) {
      try {
        ConnectionDescriptor parser = new ConnectionDescriptor(baseDir);
        ConnectionProfile profile = parser.parseDefinition(desc);
        if (profile != null) {
          return ConnectionMgr.getInstance().getConnection(profile, id);
        }
      } catch (InvalidConnectionDescriptor icd) {
        LogMgr.logError("CommandLineConnectionHandler.getConnection()", "Error connecting to database", icd);
        result.addMessage(icd.getLocalizedMessage());
        result.setFailure();
        return null;
      } catch (Exception e) {
        LogMgr.logError("CommandLineConnectionHandler.getConnection()", "Error connecting to database", e);
        result.addMessage(ResourceMgr.getFormattedString("ErrConnectDescriptor", desc));
        result.addMessage(ExceptionUtil.getDisplay(e));
        result.setFailure();
        return null;
      }
    }

    // No "short connection" specified, fallback to old profile key based connection.

    return getConnectionFromKey(currentConnection, result, id);
  }

  private WbConnection getConnectionFromKey(WbConnection currentConnection, StatementRunnerResult result, String id) {
    ProfileKey profileKey = getProfileKey();

    if (profileKey == null || (currentConnection != null && currentConnection.getProfile().isProfileForKey(profileKey))) {
      return currentConnection;
    } else {
      ConnectionProfile tprof = ConnectionMgr.getInstance().getProfile(profileKey);
      if (tprof == null) {
        String msg = ResourceMgr.getFormattedString("ErrProfileNotFound", profileKey.toString());
        result.addMessage(msg);
        result.setFailure();
        return null;
      }

      try {
        return ConnectionMgr.getInstance().getConnection(profileKey, id);
      } catch (Exception e) {
        LogMgr.logError("CommandLineConnectionHandler.getConnectionFromKey()", "Error connecting to database", e);
        result.addMessage(ResourceMgr.getFormattedString("ErrConnectProfile", profileKey.toString()));
        result.addMessage(ExceptionUtil.getDisplay(e));
        result.setFailure();
        return null;
      }
    }
  }

  public ProfileKey getProfileKey() {
    String sourceProfile = cmdLine.getValue(profileNameArgument);
    String sourceGroup = cmdLine.getValue(profileGroupArgument);
    ProfileKey key = null;
    if (sourceProfile != null) {
      key = new ProfileKey(sourceProfile, sourceGroup);
    }
    return key;
  }

}
