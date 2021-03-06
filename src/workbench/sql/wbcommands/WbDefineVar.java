/*
 * WbDefineVar.java
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

import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.sql.VariablePool;
import workbench.util.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL Command to define a variable that gets stored in the system
 * wide parameter pool.
 *
 * @author Thomas Kellerer
 * @see workbench.sql.VariablePool
 */
public class WbDefineVar
    extends SqlCommand {
  public static final String VERB = "WbVarDef";
  public static final String ARG_LOOKUP_VALUES = "values";
  public static final String ARG_REMOVE_UNDEFINED = "removeUndefined";
  public static final String ARG_REPLACE_VARS = "replaceVars";
  public static final String ARG_VAR_NAME = "variable";
  public static final String ARG_VAR_VALUE = "value";
  public static final String ARG_CONTENT_FILE = "contentFile";
  public static final String ARG_CLEANUP_VALUE = "cleanupValue";

  public WbDefineVar() {
    super();
    this.cmdLine = new ArgumentParser();
    this.cmdLine.addArgument(CommonArgs.ARG_FILE, ArgumentType.Filename);
    this.cmdLine.addArgument(ARG_CONTENT_FILE, ArgumentType.StringArgument);
    this.cmdLine.addArgument(ARG_VAR_NAME);
    this.cmdLine.addArgument(ARG_VAR_VALUE);
    this.cmdLine.addArgument(ARG_REPLACE_VARS, ArgumentType.BoolArgument);
    this.cmdLine.addArgument(ARG_REMOVE_UNDEFINED, ArgumentType.BoolSwitch);
    this.cmdLine.addArgument(ARG_CLEANUP_VALUE, ArgumentType.BoolArgument);
    this.cmdLine.addArgument(ARG_LOOKUP_VALUES);

    CommonArgs.addEncodingParameter(cmdLine);
  }

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
    String sql = getCommandLine(aSql);

    cmdLine.parse(sql);
    WbFile file = this.evaluateFileArgument(cmdLine.getValue(CommonArgs.ARG_FILE));
    WbFile contentFile = this.evaluateFileArgument(cmdLine.getValue(ARG_CONTENT_FILE));

    boolean removeUndefined = cmdLine.getBoolean(ARG_REMOVE_UNDEFINED);
    String varDef;
    if (cmdLine.hasArguments()) {
      varDef = cmdLine.getNonArguments();
    } else {
      varDef = sql;
    }

    if (file != null && contentFile != null) {
      result.addMessageByKey("ErrVarFileWrong");
      result.setFailure();
      return result;
    }

    if (file != null) {
      initFromFile(result, file);
      return result;
    }

    if (contentFile != null) {
      readFileContents(result, contentFile);
      return result;
    }

    String valueParameter = null;
    List<String> varNames = null;
    String varName = null;
    boolean lookupDefined = false;

    if (cmdLine.isArgPresent(ARG_VAR_NAME)) {
      varName = cmdLine.getValue(ARG_VAR_NAME);
      varNames = CollectionUtil.arrayList(varName);
    }

    if (cmdLine.isArgPresent(ARG_VAR_NAME) && cmdLine.isArgPresent(ARG_LOOKUP_VALUES)) {
      List<String> lookupValues = cmdLine.getListValue(ARG_LOOKUP_VALUES);
      if (CollectionUtil.isNonEmpty(lookupValues) && StringUtil.isNonEmpty(varName)) {
        LogMgr.logDebug("WbDefineVar.execute()", "Lookup values for variable " + varName + ": " + lookupValues);
        VariablePool.getInstance().setLookupValues(varName, lookupValues);
      }
      lookupDefined = true;
    }

    if (cmdLine.isArgPresent(ARG_VAR_VALUE)) {
      valueParameter = cmdLine.getValue(ARG_VAR_VALUE);
    } else if (!cmdLine.isArgPresent(ARG_VAR_NAME)) {
      WbStringTokenizer tok = new WbStringTokenizer("=", true, "\"'", false);
      tok.setSourceString(varDef);
      tok.setKeepQuotes(true);

      if (tok.hasMoreTokens()) varName = tok.nextToken();

      varNames = StringUtil.stringToList(varName, ",", true, true);

      if (tok.hasMoreTokens()) {
        valueParameter = tok.nextToken();
      }
    }

    if (CollectionUtil.isEmpty(varNames)) {
      result.addMessageByKey("ErrVarDefWrongParameter");
      result.setFailure();
      return result;
    }

    if (lookupDefined && valueParameter == null) {
      valueParameter = "";
    }

    result.setSuccess();

    if (valueParameter == null) {
      for (String name : varNames) {
        VariablePool.getInstance().removeValue(name);
        String removed = ResourceMgr.getString("MsgVarDefVariableRemoved");
        removed = removed.replace("%var%", name);
        result.addMessage(removed);
      }
    } else if (valueParameter.trim().startsWith("@") || StringUtil.trimQuotes(valueParameter).startsWith("@")) {
      readValuesFromDatabase(result, varNames, valueParameter);
    } else {
      // WbStringTokenizer returned any quotes that were used, so we have to remove them again
      // as they should not be part of the variable value
      valueParameter = StringUtil.trimQuotes(valueParameter.trim());
      boolean cleanup = cmdLine.getBoolean(ARG_CLEANUP_VALUE, Settings.getInstance().getCleanupVariableValues());
      if (cleanup) {
        valueParameter = SqlUtil.makeCleanSql(valueParameter, false, false);
      }

      if (removeUndefined) {
        // as the SQL that was passed to this command already has all variables replaced,
        // we can simply remove anything that looks like a variable in the value.
        valueParameter = VariablePool.getInstance().removeVariables(valueParameter);
      }

      if (varNames.size() > 1) {
        LogMgr.logWarning("WbDefineVar.execute()", "Multiple variables not supported when assigning constant values. Statement was: " + sql);
      }

      varName = varNames.get(0).trim();
      setVariable(result, varName, valueParameter);

      if (result.isSuccess()) {
        String msg = ResourceMgr.getString("MsgVarDefVariableDefined");
        msg = StringUtil.replace(msg, "%var%", varName);
        msg = StringUtil.replace(msg, "%value%", valueParameter);
        msg = StringUtil.replace(msg, "%varname%", VariablePool.getInstance().buildVarName(varName, false));
        result.addMessage(msg);
      }
    }

    return result;
  }

  private void readValuesFromDatabase(StatementRunnerResult result, List<String> varNames, String valueParameter) {
    String valueSql = null;
    try {
      // In case the @ sign was placed inside the quotes, make sure
      // there are no quotes before removing the @ sign
      valueParameter = StringUtil.trimQuotes(valueParameter);
      valueSql = StringUtil.trimQuotes(valueParameter.trim().substring(1));
      List<String> values = this.evaluateSql(currentConnection, valueSql, result);
      int varCount = Math.min(values.size(), varNames.size());

      if (values.size() != varNames.size()) {
        LogMgr.logWarning("WbDefineVar.execute()", "The number of variables does not match the number of columns returned. Using only the first " + varCount + " variables");
      }

      for (int i = 0; i < varCount; i++) {
        setVariable(result, varNames.get(i), values.get(i));
        if (result.isSuccess()) {
          String msg = ResourceMgr.getString("MsgVarDefVariableDefined");
          msg = StringUtil.replace(msg, "%var%", varNames.get(i));
          msg = StringUtil.replace(msg, "%value%", values.get(i));
          msg = StringUtil.replace(msg, "%varname%", VariablePool.getInstance().buildVarName(varNames.get(i), false));
          result.addMessage(msg);
        }

      }
    } catch (Exception e) {
      LogMgr.logError("WbDefineVar.execute()", "Error retrieving variable value using SQL: " + valueSql, e);
      String err = ResourceMgr.getString("ErrReadingVarSql");
      err = StringUtil.replace(err, "%sql%", valueSql);
      err = err + "\n\n" + ExceptionUtil.getDisplay(e);
      result.addMessage(err);
      result.setFailure();
    }
  }

  private void initFromFile(StatementRunnerResult result, WbFile file) {
    // if the file argument has been supplied, no variable definition
    // can be present, but the encoding parameter might have been passed
    String encoding = cmdLine.getValue("encoding");
    try {
      if (file.exists()) {
        VariablePool.getInstance().readFromFile(file.getFullPath(), encoding);
        String msg = ResourceMgr.getString("MsgVarDefFileLoaded");
        msg = StringUtil.replace(msg, "%file%", file.getFullPath());
        result.addMessage(msg);
        result.setSuccess();
      } else {
        String msg = ResourceMgr.getFormattedString("ErrFileNotFound", file.getFullPath());
        result.addMessage(msg);
        result.setFailure();
      }
    } catch (Exception e) {
      LogMgr.logError("WbDefineVar.execute()", "Error reading definition file: " + file.getFullPath(), e);
      String msg = ResourceMgr.getString("ErrReadingVarDefFile");
      msg = StringUtil.replace(msg, "%file%", file.getAbsolutePath());
      msg = msg + " " + ExceptionUtil.getDisplay(e);
      result.addMessage(msg);
      result.setFailure();
    }
  }

  private void setVariable(StatementRunnerResult result, String var, String value) {
    try {
      VariablePool.getInstance().setParameterValue(var, value);
    } catch (IllegalArgumentException e) {
      result.addMessageByKey("ErrVarDefWrongName");
      result.setFailure();
    }
  }

  /**
   * Return the result of the given SQL string and return
   * the value of the first column of the first row
   * as a string value.
   * <p/>
   * If the SQL gives an error, an empty string will be returned
   */
  private List<String> evaluateSql(WbConnection conn, String sql, StatementRunnerResult stmtResult)
      throws SQLException {
    ResultSet rs = null;
    List<String> result = new ArrayList<String>(1);
    if (conn == null) {
      throw new SQLException("Cannot evaluate SQL based variable without a connection");
    }

    try {
      this.currentStatement = conn.createStatement();

      if (sql.endsWith(";")) {
        sql = sql.substring(0, sql.length() - 1);
      }
      rs = this.currentStatement.executeQuery(sql);
      ResultSetMetaData meta = rs.getMetaData();
      int colCount = meta.getColumnCount();
      if (rs.next()) {
        for (int col = 1; col <= colCount; col++) {
          Object value = rs.getObject(col);
          if (value != null) {
            result.add(value.toString());
          }
        }
      }

      if (rs.next()) {
        stmtResult.setWarning(true);
        stmtResult.addMessageByKey("ErrVarDefRows");
      }

      if (stmtResult.hasWarning()) {
        stmtResult.addMessageNewLine();
      }
    } finally {
      SqlUtil.closeResult(rs);
    }

    return result;
  }

  private void readFileContents(StatementRunnerResult result, WbFile contentFile) {
    String varname = cmdLine.getValue(ARG_VAR_NAME);
    if (StringUtil.isBlank(varname)) {
      result.addMessageByKey("ErrVarNoName");
      result.setFailure();
      return;
    }

    boolean replace = cmdLine.getBoolean(ARG_REPLACE_VARS, true);
    String encoding = cmdLine.getValue("encoding");
    if (encoding == null) {
      encoding = EncodingUtil.getDefaultEncoding();
    }

    try {
      String value = FileUtil.readFile(contentFile, encoding);
      if (replace) {
        value = VariablePool.getInstance().replaceAllParameters(value);
      }

      setVariable(result, varname, value);
      String msg = ResourceMgr.getFormattedString("MsgVarReadFile", varname, contentFile.getFullPath());
      result.addMessage(msg);
    } catch (FileNotFoundException fnf) {
      LogMgr.logError("WbDefineVar.execute()", "Content file " + contentFile.getFullPath() + " not found!", fnf);
      result.addMessage(ResourceMgr.getFormattedString("ErrFileNotFound", contentFile.getFullPath()));
      result.setFailure();
    } catch (IOException io) {
      result.addMessage(ExceptionUtil.getDisplay(io));
      result.setFailure();
    }
  }

  @Override
  public boolean isWbCommand() {
    return true;
  }
}
