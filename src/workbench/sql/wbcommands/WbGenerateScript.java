/*
 * WbGenerateScript.java
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

import workbench.db.*;
import workbench.interfaces.ScriptGenerationMonitor;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.storage.RowActionMonitor;
import workbench.util.*;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Kellerer
 */
public class WbGenerateScript
    extends SqlCommand
    implements ScriptGenerationMonitor {
  public static final String VERB = "WbGenerateScript";
  public static final String SHORT_VERB = "WbGenScript";
  public static final String ARG_EXCLUDE = "exclude";

  private ObjectScripter scripter;

  public WbGenerateScript() {
    super();
    this.isUpdatingCommand = false;

    cmdLine = new ArgumentParser();
    cmdLine.addArgument(CommonArgs.ARG_TYPES, ArgumentType.ObjectTypeArgument);
    cmdLine.addArgument(CommonArgs.ARG_SCHEMAS, ArgumentType.SchemaArgument);
    cmdLine.addArgument(CommonArgs.ARG_OBJECTS, ArgumentType.TableArgument);
    cmdLine.addArgument(ARG_EXCLUDE);
    cmdLine.addArgument(WbSchemaReport.ARG_INCLUDE_PROCS, ArgumentType.BoolSwitch);
    cmdLine.addArgument(WbSchemaReport.ARG_INCLUDE_TRIGGERS, ArgumentType.BoolSwitch);
    cmdLine.addArgument(WbSchemaReport.ARG_INCLUDE_GRANTS, ArgumentType.BoolArgument);
    cmdLine.addArgument("useSeparator", ArgumentType.BoolSwitch);
    cmdLine.addArgument(CommonArgs.ARG_FILE, ArgumentType.Filename);
    cmdLine.addArgument("includeDrop", ArgumentType.BoolSwitch);
    CommonArgs.addEncodingParameter(cmdLine);
  }

  @Override
  public StatementRunnerResult execute(String sql)
      throws SQLException {
    StatementRunnerResult result = new StatementRunnerResult();
    String args = getCommandLine(sql);
    cmdLine.parse(args);

    List<String> schemas = null;
    List<String> types = null;
    String names = null;

    if (!cmdLine.hasArguments()) {
      names = args;
    } else {
      if (cmdLine.hasUnknownArguments()) {
        setUnknownMessage(result, cmdLine, ResourceMgr.getString("ErrGenScriptWrongParam"));
        return result;
      }
      names = cmdLine.getValue(CommonArgs.ARG_OBJECTS);
      schemas = cmdLine.getListValue(CommonArgs.ARG_SCHEMAS);
      types = cmdLine.getListValue(CommonArgs.ARG_TYPES);
    }

    List<DbObject> objects = new ArrayList<DbObject>();

    if (CollectionUtil.isEmpty(schemas)) {
      schemas = CollectionUtil.arrayList(currentConnection.getCurrentSchema());
    }

    String excluded = cmdLine.getValue(ARG_EXCLUDE);
    String[] typesArray = CollectionUtil.isEmpty(types) ? null : StringUtil.toArray(types, true, true);

    for (String schema : schemas) {
      SourceTableArgument selector = new SourceTableArgument(names, excluded, schema, typesArray, currentConnection);
      objects.addAll(selector.getTables());
    }

    if (cmdLine.getBoolean(WbSchemaReport.ARG_INCLUDE_PROCS, false)) {
      ProcedureReader reader = currentConnection.getMetadata().getProcedureReader();
      for (String schema : schemas) {
        if (isCancelled) break;
        List<ProcedureDefinition> procs = reader.getProcedureList(null, schema, null);
        objects.addAll(procs);
      }
    }

    if (cmdLine.getBoolean(WbSchemaReport.ARG_INCLUDE_TRIGGERS, false)) {
      TriggerReader reader = TriggerReaderFactory.createReader(currentConnection);
      for (String schema : schemas) {
        if (isCancelled) break;
        List<TriggerDefinition> triggers = reader.getTriggerList(null, schema, null);
        objects.addAll(triggers);
      }
    }

    if (isCancelled) {
      result.setWarning(true);
      return result;
    }

    WbFile output = evaluateFileArgument(cmdLine.getValue(CommonArgs.ARG_FILE));

    scripter = new ObjectScripter(objects, currentConnection);
    scripter.setUseSeparator(cmdLine.getBoolean("useSeparator", false));
    scripter.setIncludeDrop(cmdLine.getBoolean("includeDrop", false));
    scripter.setIncludeGrants(cmdLine.getBoolean(WbSchemaReport.ARG_INCLUDE_GRANTS, true));

    if (this.rowMonitor != null) {
      rowMonitor.saveCurrentType("genscript");
      rowMonitor.setMonitorType(RowActionMonitor.MONITOR_PROCESS_TABLE);
    }
    scripter.setProgressMonitor(this);
    try {
      scripter.generateScript();
    } finally {
      if (rowMonitor != null) {
        rowMonitor.restoreType("genscript");
        rowMonitor.jobFinished();
      }
    }

    if (isCancelled) {
      result.setWarning(true);
      return result;
    }

    result.setSuccess();

    String encoding = cmdLine.getValue(CommonArgs.ARG_ENCODING, EncodingUtil.getDefaultEncoding());
    if (output != null) {
      Writer writer = null;
      try {
        writer = EncodingUtil.createWriter(output, encoding, false);
        writer.write(scripter.getScript());
        result.addMessage(ResourceMgr.getFormattedString("MsgScriptWritten", output.getAbsolutePath()));
      } catch (IOException io) {
        LogMgr.logError("WbGenerateScript.execute()", "Could not write outputfile", io);
        result.setFailure();
        result.addMessage(io.getLocalizedMessage());
      } finally {
        FileUtil.closeQuietely(writer);
      }
    } else {
      result.addMessage(scripter.getScript());
    }
    return result;
  }

  @Override
  public void cancel()
      throws SQLException {
    super.cancel();
    if (scripter != null) {
      scripter.cancel();
    }
  }

  @Override
  public String getVerb() {
    return VERB;
  }

  @Override
  public String getAlternateVerb() {
    return SHORT_VERB;
  }

  @Override
  public void done() {
    scripter = null;
  }

  @Override
  public void setCurrentObject(String anObject, int current, int count) {
    if (this.rowMonitor != null) {
      if (anObject.indexOf(' ') > -1) {
        try {
          rowMonitor.saveCurrentType("gen2");
          rowMonitor.setMonitorType(RowActionMonitor.MONITOR_PLAIN);
          rowMonitor.setCurrentObject(anObject, current, count);
        } finally {
          rowMonitor.restoreType("gen2");
        }
      } else {
        rowMonitor.setCurrentObject(anObject, current, count);
      }
    }
  }

  @Override
  public boolean isWbCommand() {
    return true;
  }

}
