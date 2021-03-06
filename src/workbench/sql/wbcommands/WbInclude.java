/*
 * WbInclude.java
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

import workbench.AppArguments;
import workbench.WbManager;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.sql.BatchRunner;
import workbench.sql.DelimiterDefinition;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.storage.DataStore;
import workbench.util.*;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Thomas Kellerer
 */
public class WbInclude
    extends SqlCommand {
  public static final String VERB = "WbInclude";
  public static final String ORA_INCLUDE = "@";

  public static final String ARG_SEARCH_VALUE = "searchFor";
  public static final String ARG_REPLACE_VALUE = "replaceWith";
  public static final String ARG_REPLACE_USE_REGEX = "useRegex";
  public static final String ARG_REPLACE_IGNORECASE = "ignoreCase";
  public static final String ARG_CHECK_ESCAPED_QUOTES = "checkEscapedQuotes";
  public static final String ARG_PRINT_STATEMENTS = "printStatements";
  public static final String ARG_DELIMITER = "delimiter";

  /*
   * I need to store the instance in a variable to be able to cancel the execution.
   * If cancelling wasn't necessary, a local variable in the execute() method would have been enough.
   */
  private BatchRunner batchRunner;

  public WbInclude() {
    super();
    cmdLine = new ArgumentParser();
    cmdLine.addArgument(CommonArgs.ARG_FILE, ArgumentType.Filename);
    cmdLine.addArgument(CommonArgs.ARG_CONTINUE, ArgumentType.BoolArgument);
    cmdLine.addArgument(AppArguments.ARG_SHOWPROGRESS, ArgumentType.BoolArgument);
    cmdLine.addArgument(AppArguments.ARG_DISPLAY_RESULT, ArgumentType.BoolArgument);
    cmdLine.addArgument(ARG_CHECK_ESCAPED_QUOTES, ArgumentType.BoolArgument);
    cmdLine.addArgument(ARG_DELIMITER, StringUtil.stringToList("';',oracle,mssql"));
    cmdLine.addArgument(CommonArgs.ARG_VERBOSE, ArgumentType.BoolSwitch);
    ConditionCheck.addParameters(cmdLine);
    cmdLine.addArgument(AppArguments.ARG_IGNORE_DROP, ArgumentType.BoolArgument);
    cmdLine.addArgument(WbImport.ARG_USE_SAVEPOINT, ArgumentType.BoolArgument);
    cmdLine.addArgument(ARG_SEARCH_VALUE);
    cmdLine.addArgument(ARG_REPLACE_VALUE);
    cmdLine.addArgument(ARG_REPLACE_USE_REGEX, ArgumentType.BoolSwitch);
    cmdLine.addArgument(ARG_REPLACE_IGNORECASE, ArgumentType.BoolSwitch);
    cmdLine.addArgument(ARG_PRINT_STATEMENTS, ArgumentType.BoolSwitch);
    cmdLine.addArgument(AppArguments.ARG_SHOW_TIMING, ArgumentType.BoolSwitch);
    CommonArgs.addEncodingParameter(cmdLine);
  }

  @Override
  public String getVerb() {
    return VERB;
  }

  @Override
  public String getAlternateVerb() {
    return ORA_INCLUDE;
  }

  @Override
  protected boolean isConnectionRequired() {
    return false;
  }

  @Override
  public StatementRunnerResult execute(String aSql)
      throws SQLException {
    StatementRunnerResult result = new StatementRunnerResult(aSql);
    result.setSuccess();

    String clean = SqlUtil.makeCleanSql(aSql, false, false);
    boolean checkParms = true;
    boolean showProgress = true;

    WbFile file = null;

    if (clean.charAt(0) == '@') {
      clean = clean.substring(1);
      file = evaluateFileArgument(clean);
      checkParms = false;
      showProgress = false;
    } else {
      clean = getCommandLine(aSql);
      cmdLine.parse(clean);
      file = evaluateFileArgument(cmdLine.getValue(CommonArgs.ARG_FILE));
      if (file == null) {
        // support a short version of WbInclude that simply specifies the filename
        file = evaluateFileArgument(clean);
        checkParms = false;
      }
    }

    if (file != null && StringUtil.isEmptyString(file.getExtension())) {
      file = new WbFile(file.getFullPath() + ".sql");
    }

    if (!ConditionCheck.isCommandLineOK(result, cmdLine)) {
      return result;
    }

    if (!checkConditions(result)) {
      return result;
    }

    if (file == null) {
      String msg = ResourceMgr.getString("ErrIncludeWrongParameter").
          replace("%default_encoding%", Settings.getInstance().getDefaultEncoding()).
          replace("%default_continue%", Boolean.toString(Settings.getInstance().getIncludeDefaultContinue()));
      result.addMessage(msg);
      result.setFailure();
      return result;
    }

    List<File> allFiles = null;
    if (FileUtil.hasWildcard(cmdLine.getValue(CommonArgs.ARG_FILE))) {
      String search = StringUtil.trimQuotes(cmdLine.getValue(CommonArgs.ARG_FILE));
      File f = new File(search);
      if (f.getParentFile() == null && this.runner != null) {
        String dir = this.runner.getBaseDir();
        if (StringUtil.isNonEmpty(dir)) {
          f = new File(dir, search);
          search = f.getPath();
        }
      }
      allFiles = FileUtil.listFiles(search);
      if (allFiles.isEmpty()) {
        result.setFailure();
        String msg = ResourceMgr.getFormattedString("ErrFileNotFound", search);
        result.addMessage(msg);
        return result;
      }
    } else if (!file.exists()) {
      result.setFailure();
      String msg = ResourceMgr.getFormattedString("ErrFileNotFound", file.getFullPath());
      result.addMessage(msg);
      return result;
    }

    boolean continueOnError = false;
    boolean checkEscape = Settings.getInstance().getCheckEscapedQuotes();
    boolean verbose = true;
    boolean defaultIgnore = (currentConnection == null ? false : currentConnection.getProfile().getIgnoreDropErrors());
    boolean ignoreDrop = defaultIgnore;
    boolean showStmts = false;
    boolean showTiming = false;
    DelimiterDefinition delim = null;
    String encoding = null;

    if (checkParms) {
      continueOnError = cmdLine.getBoolean(CommonArgs.ARG_CONTINUE, Settings.getInstance().getIncludeDefaultContinue());
      checkEscape = cmdLine.getBoolean(ARG_CHECK_ESCAPED_QUOTES, Settings.getInstance().getCheckEscapedQuotes());
      verbose = cmdLine.getBoolean(CommonArgs.ARG_VERBOSE, false);
      defaultIgnore = (currentConnection == null ? false : currentConnection.getProfile().getIgnoreDropErrors());
      ignoreDrop = cmdLine.getBoolean(AppArguments.ARG_IGNORE_DROP, defaultIgnore);
      encoding = cmdLine.getValue(CommonArgs.ARG_ENCODING);
      delim = DelimiterDefinition.parseCmdLineArgument(cmdLine.getValue(ARG_DELIMITER));
      setUnknownMessage(result, cmdLine, null);
      showStmts = cmdLine.getBoolean(ARG_PRINT_STATEMENTS, this.runner.getTraceStatements());
      showTiming = cmdLine.getBoolean(AppArguments.ARG_SHOW_TIMING, false);
      showProgress = cmdLine.getBoolean(AppArguments.ARG_SHOWPROGRESS, true);
    }

    if (encoding == null) {
      encoding = Settings.getInstance().getDefaultEncoding();
    }

    try {
      if (CollectionUtil.isEmpty(allFiles)) {
        batchRunner = new BatchRunner(file.getCanonicalPath());
        String dir = file.getCanonicalFile().getParent();
        batchRunner.setBaseDir(dir);
      } else {
        batchRunner = new BatchRunner(allFiles);
      }

      batchRunner.setConnection(currentConnection);
      batchRunner.setDelimiter(delim);
      batchRunner.setResultLogger(this.resultLogger);
      batchRunner.setVerboseLogging(verbose);
      if (showProgress) {
        batchRunner.setRowMonitor(this.rowMonitor);
      }
      batchRunner.setShowProgress(showProgress);
      batchRunner.setAbortOnError(!continueOnError);
      batchRunner.setCheckEscapedQuotes(checkEscape);
      batchRunner.setShowTiming(showTiming);
      batchRunner.setPrintStatements(showStmts);
      batchRunner.setEncoding(encoding);
      batchRunner.setParameterPrompter(this.prompter);
      batchRunner.setExecutionController(runner.getExecutionController());
      batchRunner.setIgnoreDropErrors(ignoreDrop);
      boolean showResults = cmdLine.getBoolean(AppArguments.ARG_DISPLAY_RESULT, false);
      batchRunner.showResultSets(showResults);
      batchRunner.setOptimizeColWidths(showResults);
      if (cmdLine.isArgPresent(WbImport.ARG_USE_SAVEPOINT)) {
        batchRunner.setUseSavepoint(cmdLine.getBoolean(WbImport.ARG_USE_SAVEPOINT));
      }
      batchRunner.setReplacer(getReplacer());

      if (showResults) {
        if (WbManager.getInstance().isGUIMode()) {
          // Make sure the batchRunner doesn't print the results to System.out
          batchRunner.setConsole(null);
        } else {
          batchRunner.setShowProgress(false);
        }
      }

      batchRunner.execute();

      if (batchRunner.isSuccess()) {
        result.setSuccess();
      } else {
        result.setFailure();
      }

      List<DataStore> results = batchRunner.getQueryResults();
      for (DataStore ds : results) {
        result.addDataStore(ds);
      }

      if (this.rowMonitor != null) {
        this.rowMonitor.jobFinished();
      }
    } catch (Exception th) {
      result.setFailure();
      result.addMessage(ExceptionUtil.getDisplay(th));
    }
    return result;
  }

  private Replacer getReplacer() {
    String searchValue = cmdLine.getValue(ARG_SEARCH_VALUE);
    if (StringUtil.isBlank(searchValue)) return null;

    if (!cmdLine.isArgPresent(ARG_REPLACE_VALUE)) {
      return null;
    }
    boolean useRegex = cmdLine.getBoolean(ARG_REPLACE_USE_REGEX, false);
    boolean ignoreCase = cmdLine.getBoolean(ARG_REPLACE_IGNORECASE, true);
    String replace = cmdLine.getValue(ARG_REPLACE_VALUE);
    return new Replacer(searchValue, replace, ignoreCase, useRegex);
  }

  @Override
  public void done() {
    if (batchRunner != null) {
      batchRunner.done();
      batchRunner = null;
    }
  }

  @Override
  public void cancel()
      throws SQLException {
    super.cancel();
    if (batchRunner != null) {
      batchRunner.cancel();
    }
  }

  private boolean checkConditions(StatementRunnerResult result) {
    ConditionCheck.Result check = ConditionCheck.checkConditions(cmdLine);
    if (check.isOK()) {
      return true;
    }
    result.addMessage(ConditionCheck.getMessage("ErrInclude", check));
    result.setSuccess();
    return false;
  }

  @Override
  public boolean isWbCommand() {
    return true;
  }
}
