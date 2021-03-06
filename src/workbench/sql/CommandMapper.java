/*
 * CommandMapper.java
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
package workbench.sql;

import workbench.db.DbMetadata;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.sql.commands.*;
import workbench.sql.wbcommands.*;
import workbench.sql.wbcommands.console.*;
import workbench.util.CaseInsensitiveComparator;
import workbench.util.CollectionUtil;
import workbench.util.SqlParsingUtil;
import workbench.util.StringUtil;

import java.util.*;

/**
 * @author Thomas Kellerer
 */
public class CommandMapper {
  private final Map<String, SqlCommand> cmdDispatch;
  private final List<String> dbSpecificCommands;
  private final Set<String> passThrough = CollectionUtil.caseInsensitiveSet();
  private final boolean allowAbbreviated;
  private boolean supportsSelectInto;
  private DbMetadata metaData;

  public CommandMapper() {
    cmdDispatch = new TreeMap<String, SqlCommand>(CaseInsensitiveComparator.INSTANCE);
    cmdDispatch.put("*", new SqlCommand());

    // Workbench specific commands
    addCommand(new WbList());
    addCommand(new WbListProcedures());
    addCommand(new WbDefineVar());
    addCommand(new WbEnableOraOutput());
    addCommand(new WbDisableOraOutput());
    addCommand(new WbStartBatch());
    addCommand(new WbEndBatch());
    addCommand(new WbXslt());
    addCommand(new WbRemoveVar());
    addCommand(new WbListVars());
    addCommand(new WbExport());
    addCommand(new WbImport());
    addCommand(new WbCopy());
    addCommand(new WbSchemaReport());
    addCommand(new WbSchemaDiff());
    addCommand(new WbDataDiff());
    addCommand(new WbFeedback());
    addCommand(new WbDefinePk());
    addCommand(new WbListPkDef());
    addCommand(new WbLoadPkMapping());
    addCommand(new WbSavePkMapping());
    addCommand(new WbConfirm());
    addCommand(new WbCall());
    addCommand(new WbConnect());
    addCommand(new WbInclude());
    addCommand(new WbListCatalogs());
    addCommand(new WbListSchemas());
    addCommand(new WbHelp());
    addCommand(new WbSelectBlob());
    addCommand(new WbHideWarnings());
    addCommand(new WbProcSource());
    addCommand(new WbListTriggers());
    addCommand(new WbListIndexes());
    addCommand(new WbTriggerSource());
    addCommand(new WbViewSource());
    addCommand(new WbTableSource());
    addCommand(new WbDescribeObject());
    addCommand(new WbGrepSource());
    addCommand(new WbGrepData());
    addCommand(new WbMode());
    addCommand(new WbFetchSize());
    addCommand(new WbAbout());
    addCommand(new WbRunLB());
    addCommand(new WbIsolationLevel());
    addCommand(new WbConnInfo());
    addCommand(new WbSysExec());
    addCommand(new WbSysOpen());
    addCommand(new WbShowProps());
    addCommand(new WbSetProp());
    addCommand(new WbGenDrop());
    addCommand(new WbGenerateScript());
    addCommand(new WbGenDelete());
    addCommand(new WbGenInsert());
    addCommand(new WbEcho());
    addCommand(new WbShowEncoding());
    addCommand(new WbRowCount());

    addCommand(new WbDisconnect());
    addCommand(new WbDisplay());
    addCommand(new WbToggleDisplay());
    addCommand(new WbRun());
    addCommand(new WbHistory());
    addCommand(new WbListMacros());
    addCommand(new WbDefineMacro());
    addCommand(new WbDeleteMacro());

    addCommand(new WbStoreProfile());
    addCommand(new WbDeleteProfile());
    addCommand(new WbCreateProfile());
    addCommand(new WbDefineDriver());
    addCommand(new WbListProfiles());
    addCommand(new WbListDrivers());

    // Wrappers for standard SQL statements
    addCommand(SingleVerbCommand.getCommit());
    addCommand(SingleVerbCommand.getRollback());

    addCommand(UpdatingCommand.getDeleteCommand());
    addCommand(UpdatingCommand.getInsertCommand());
    addCommand(UpdatingCommand.getUpdateCommand());
    addCommand(UpdatingCommand.getTruncateCommand());

    addCommand(new SetCommand());
    addCommand(new SelectCommand());

    for (DdlCommand cmd : DdlCommand.getDdlCommands()) {
      addCommand(cmd);
    }
    this.cmdDispatch.put("CREATE OR REPLACE", DdlCommand.getCreateCommand());

    this.dbSpecificCommands = new LinkedList<String>();
    this.allowAbbreviated = Settings.getInstance().getBoolProperty("workbench.sql.allow.abbreviation", false);
    registerExtensions();
  }

  private void registerExtensions() {
    List<SqlCommand> commands = CommandRegistry.getInstance().getCommands();
    for (SqlCommand cmd : commands) {
      addCommand(cmd);
    }
  }

  public Collection<String> getAllWbCommands() {
    Collection<SqlCommand> commands = cmdDispatch.values();
    TreeSet<String> result = new TreeSet<String>();
    for (SqlCommand cmd : commands) {
      if (cmd.isWbCommand()) {
        result.add(cmd.getVerb());
        if (cmd.getAlternateVerb() != null) {
          result.add(cmd.getAlternateVerb());
        }
      }
    }
    return result;
  }

  /**
   * Add a new command definition during runtime.
   */
  public final void addCommand(SqlCommand command) {
    cmdDispatch.put(command.getVerb(), command);
    String alternate = command.getAlternateVerb();
    if (alternate != null) {
      cmdDispatch.put(alternate.toUpperCase(), command);
    }
  }

  /**
   * Initialize the CommandMapper with a database connection.
   * This will add DBMS specific commands to the internal dispatch.
   * <p/>
   * This method can be called multiple times.
   */
  public void setConnection(WbConnection aConn) {
    this.cmdDispatch.keySet().removeAll(dbSpecificCommands);
    this.dbSpecificCommands.clear();
    this.supportsSelectInto = false;

    if (aConn == null) return;

    this.metaData = aConn.getMetadata();

    if (metaData == null) {
      LogMgr.logError("CommandMapper.setConnection()", "Received connection without metaData!", null);
      return;
    }

    if (metaData.isOracle()) {
      SqlCommand wbcall = this.cmdDispatch.get(WbCall.VERB);

      this.cmdDispatch.put(WbCall.EXEC_VERB_LONG, wbcall);
      this.cmdDispatch.put(WbCall.EXEC_VERB_SHORT, wbcall);

      AlterSessionCommand alter = new AlterSessionCommand();
      this.cmdDispatch.put(alter.getVerb(), alter);
      this.cmdDispatch.put(WbOraShow.VERB, new WbOraShow());

      WbFeedback echo = new WbFeedback("ECHO");
      this.cmdDispatch.put(echo.getVerb(), echo);

      SqlCommand wbEcho = this.cmdDispatch.get(WbEcho.VERB);
      this.cmdDispatch.put("prompt", wbEcho);

      SqlCommand confirm = this.cmdDispatch.get(WbConfirm.VERB);
      this.cmdDispatch.put("pause", confirm);

      this.dbSpecificCommands.add("pause");
      this.dbSpecificCommands.add("prompt");
      this.dbSpecificCommands.add(alter.getVerb());
      this.dbSpecificCommands.add(WbCall.EXEC_VERB_LONG);
      this.dbSpecificCommands.add(WbCall.EXEC_VERB_SHORT);
      this.dbSpecificCommands.add(echo.getVerb());
      this.dbSpecificCommands.add(WbOraShow.VERB);
    } else if (metaData.isSqlServer() || metaData.isMySql()) {
      UseCommand cmd = new UseCommand();
      this.cmdDispatch.put(cmd.getVerb(), cmd);
      this.dbSpecificCommands.add(cmd.getVerb());
    } else if (metaData.isFirebird()) {
      DdlCommand recreate = DdlCommand.getRecreateCommand();
      this.cmdDispatch.put(recreate.getVerb(), recreate);
      this.dbSpecificCommands.add(recreate.getVerb());
    } else if (metaData.isPostgres()) {
      mapPsql();
    }

    if (metaData.isMySql()) {
      MySQLShow show = new MySQLShow();
      this.cmdDispatch.put(show.getVerb(), show);
      this.dbSpecificCommands.add(show.getVerb());
    }

    if (metaData.getDbSettings().useWbProcedureCall()) {
      SqlCommand wbcall = this.cmdDispatch.get(WbCall.VERB);
      this.cmdDispatch.put("CALL", wbcall);
      this.dbSpecificCommands.add("CALL");
    }

    List<String> verbs = Settings.getInstance().getListProperty("workbench.db.ignore." + metaData.getDbId(), false, "");
    for (String verb : verbs) {
      if (verb == null) continue;
      IgnoredCommand cmd = new IgnoredCommand(verb);
      this.cmdDispatch.put(verb, cmd);
      this.dbSpecificCommands.add(verb);
    }

    List<String> passVerbs = Settings.getInstance().getListProperty("workbench.db." + metaData.getDbId() + ".passthrough", false, "");
    passThrough.clear();
    if (passVerbs != null) {
      for (String v : passVerbs) {
        passThrough.add(v);
      }

    }

    // this is stored in an instance variable for performance
    // reasons, so we can skip the call to isSelectIntoNewTable() in
    // getCommandToUse()
    // For a single call this doesn't matter, but when executing
    // huge scripts the repeated call to getCommandToUse should
    // be as quick as possible
    this.supportsSelectInto = metaData.supportsSelectIntoNewTable();
  }

  private void mapPsql() {
    SqlCommand connInfo = this.cmdDispatch.get(WbConnInfo.VERB);
    this.cmdDispatch.put("\\conninfo", connInfo);
    this.dbSpecificCommands.add("\\conninfo");

    SqlCommand set = this.cmdDispatch.get(SetCommand.VERB);
    this.cmdDispatch.put("\\set", set);
    this.dbSpecificCommands.add("\\set");

    SqlCommand include = this.cmdDispatch.get(WbInclude.VERB);
    this.cmdDispatch.put("\\i", include);
    this.dbSpecificCommands.add("\\i");

    SqlCommand echo = this.cmdDispatch.get(WbEcho.VERB);
    this.cmdDispatch.put("\\echo", echo);
    this.cmdDispatch.put("\\qecho", echo);
    this.dbSpecificCommands.add("\\echo");
    this.dbSpecificCommands.add("\\qecho");

    SqlCommand help = this.cmdDispatch.get(WbHelp.VERB);
    this.cmdDispatch.put("\\help", help);
    this.cmdDispatch.put("\\h", help);
    this.dbSpecificCommands.add("\\help");
    this.dbSpecificCommands.add("\\h");
  }

  /**
   * Check for a SELECT ... INTO syntax for Informix which actually
   * creates a table. In that case we will simply pretend it's a
   * CREATE statement.
   * In all other casese, the approriate SqlCommand from commandDispatch will be used
   * This is made public in order to be accessible from a JUnit test
   *
   * @param sql the statement to be executed
   * @return the instance of SqlCommand to be used to run the sql, or null if the
   * given sql is empty or contains comments only
   */
  public SqlCommand getCommandToUse(String sql) {
    SqlCommand cmd = null;

    WbConnection conn = metaData == null ? null : metaData.getWbConnection();
    String verb = SqlParsingUtil.getInstance(conn).getSqlVerb(sql);

    if (StringUtil.isEmptyString(verb)) return null;

    if (this.supportsSelectInto && "SELECT".equals(verb) && this.metaData != null && this.metaData.isSelectIntoNewTable(sql)) {
      LogMgr.logDebug("CommandMapper.getCommandToUse()", "Found 'SELECT ... INTO new_table'");
      // use the generic SqlCommand implementation for this and not the SelectCommand
      cmd = this.cmdDispatch.get("*");
    }

    // checking for the collection size before checking for the presence
    // is a bit faster because of the hashing that is necessary to look up
    // the entry. Again this doesn't matter for a single command, but when
    // running a large script this does make a difference
    else if (passThrough.size() > 0 && passThrough.contains(verb)) {
      cmd = this.cmdDispatch.get("*");
    } else {
      cmd = this.cmdDispatch.get(verb);
    }

    if (cmd == null && allowAbbreviated) {
      Set<String> verbs = cmdDispatch.keySet();
      int found = 0;
      String lastVerb = null;
      String lverb = verb.toLowerCase();
      for (String toTest : verbs) {
        if (cmdDispatch.get(toTest).isWbCommand()) {
          if (toTest.toLowerCase().startsWith(lverb)) {
            lastVerb = toTest;
            found++;
          }
        }
      }
      if (found == 1) {
        LogMgr.logDebug("CommandMapper.getCommandToUse()", "Found workbench command " + lastVerb + " for abbreviation " + verb);
        cmd = cmdDispatch.get(lastVerb);
      }
    }

    if (cmd == null) {
      cmd = this.cmdDispatch.get("*");
    }
    return cmd;
  }

}
