/*
 * GuiSettings.java
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
package workbench.resource;

import workbench.db.WbConnection;

import static workbench.resource.Settings.PROPERTY_DBEXP_REMEMBER_SORT;


/**
 * @author Thomas Kellerer
 */
public class DbExplorerSettings {
  public static final String PROP_USE_FILTER_RETRIEVE = "workbench.dbexplorer.tablelist.filter.retrieve";
  public static final String PROP_USE_SQLSORT = "workbench.dbexplorer.datapanel.applysqlorder";
  public static final String PROP_TABLE_HISTORY = "workbench.dbexplorer.tablelist.history";
  public static final String PROP_ALLOW_ALTER_TABLE = "workbench.dbexplorer.allow.alter";

  public static final String PROP_INSTANT_FILTER = "workbench.dbexplorer.instantfilter";
  public static final String PROP_ASSUME_WILDCARDS = "workbench.dbexplorer.assumewildcards";

  public static boolean showApplyDDLHint() {
    return Settings.getInstance().getBoolProperty("workbench.gui.apply.ddl.hint", true);
  }

  public static boolean showSynonymTargetInDbExplorer() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.synonyms.showtarget", true);
  }

  public static void setShowSynonymTargetInDbExplorer(boolean flag) {
    Settings.getInstance().setProperty("workbench.dbexplorer.synonyms.showtarget", flag);
  }

  public static boolean allowAlterInDbExplorer() {
    return Settings.getInstance().getBoolProperty(PROP_ALLOW_ALTER_TABLE, false);
  }

  public static void setAllowAlterInDbExplorer(boolean flag) {
    Settings.getInstance().setProperty(PROP_ALLOW_ALTER_TABLE, flag);
  }

  public static boolean getUseFilterForRetrieve() {
    return Settings.getInstance().getBoolProperty(PROP_USE_FILTER_RETRIEVE, false);
  }

  public static void setUseFilterForRetrieve(boolean flag) {
    Settings.getInstance().setProperty(PROP_USE_FILTER_RETRIEVE, flag);
  }

  public static boolean getApplySQLSortInDbExplorer() {
    return Settings.getInstance().getBoolProperty(PROP_USE_SQLSORT, false);
  }

  public static void setApplySQLSortInDbExplorer(boolean flag) {
    Settings.getInstance().setProperty(PROP_USE_SQLSORT, flag);
  }

  public static boolean getDbExplorerTableDetailFullyQualified() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.tabledetails.fullname", true);
  }

  public static int getDbExplorerTableHistorySize() {
    return Settings.getInstance().getIntProperty("workbench.dbexplorer.tablelist.history.size", 25);
  }

  public static boolean getDbExplorerShowTableHistory() {
    return Settings.getInstance().getBoolProperty(PROP_TABLE_HISTORY, true);
  }

  public static void setDbExplorerShowTableHistory(boolean flag) {
    Settings.getInstance().setProperty(PROP_TABLE_HISTORY, flag);
  }

  public static boolean getDbExplorerMultiSelectTypes() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.tablelist.types.multiselect", true);
  }

  public static boolean getDbExplorerMultiSelectTypesAutoClose() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.tablelist.types.multiselect.autoclose", false);
  }

  public static boolean getDbExplorerIncludeTrgInTableSource() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.tablesource.include.trigger", false);
  }

  public static void setDbExplorerIncludeTrgInTableSource(boolean flag) {
    Settings.getInstance().setProperty("workbench.dbexplorer.tablesource.include.trigger", flag);
  }

  public static boolean getGenerateTableGrants() {
    return Settings.getInstance().getBoolProperty("workbench.db.generate.tablesource.include.grants", true);
  }

  public static void setGenerateTableGrants(boolean flag) {
    Settings.getInstance().setProperty("workbench.db.generate.tablesource.include.grants", flag);
  }

  public static boolean getDbExpGenerateDrop() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.generate.drop", true);
  }

  public static void setDbExpGenerateDrop(boolean flag) {
    Settings.getInstance().setProperty("workbench.dbexplorer.generate.drop", flag);
  }

  public static boolean getDbExpUsePartialMatch() {
    return Settings.getInstance().getBoolProperty(PROP_ASSUME_WILDCARDS, true);
  }

  public static void setDbExpUsePartialMatch(boolean flag) {
    Settings.getInstance().setProperty(PROP_ASSUME_WILDCARDS, flag);
  }

  public static boolean getDbExpFilterDuringTyping() {
    return Settings.getInstance().getBoolProperty(PROP_INSTANT_FILTER, false);
  }

  public static void setDbExpFilterDuringTyping(boolean flag) {
    Settings.getInstance().setProperty(PROP_INSTANT_FILTER, flag);
  }

  public static boolean isOwnTransaction(WbConnection dbConnection) {
    if (dbConnection == null) return false;
    if (dbConnection.getAutoCommit()) return false;
    return (dbConnection.getProfile().getUseSeparateConnectionPerTab() || getAlwaysUseSeparateConnForDbExpWindow());
  }

  public static boolean getAlwaysUseSeparateConnForDbExpWindow() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.connection.always.separate", false);
  }

  public static String getDefaultExplorerObjectType() {
    return Settings.getInstance().getProperty("workbench.gui.dbobjects.TableListPanel.objecttype", null);
  }

  public static void setDefaultExplorerObjectType(String type) {
    Settings.getInstance().setProperty("workbench.gui.dbobjects.TableListPanel.objecttype", type);
  }

  public static boolean getRetrieveDbExplorer() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.retrieveonopen", true);
  }

  public static void setRetrieveDbExplorer(boolean aFlag) {
    Settings.getInstance().setProperty("workbench.dbexplorer.retrieveonopen", aFlag);
  }

  public static boolean getShowDbExplorerInMainWindow() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.mainwindow", true);
  }

  public static void setShowDbExplorerInMainWindow(boolean showWindow) {
    Settings.getInstance().setProperty("workbench.dbexplorer.mainwindow", showWindow);
  }

  public static boolean getAutoGeneratePKName() {
    return Settings.getInstance().getBoolProperty("workbench.db.createpkname", false);
  }

  public static void setAutoGeneratePKName(boolean flag) {
    Settings.getInstance().setProperty("workbench.db.createpkname", flag);
  }

  public static boolean getGenerateColumnListInViews() {
    return Settings.getInstance().getBoolProperty("workbench.sql.create.view.columnlist", true);
  }

  public static void setGenerateColumnListInViews(boolean flag) {
    Settings.getInstance().setProperty("workbench.sql.create.view.columnlist", flag);
  }

  /**
   * Returns true if the DbExplorer should show an additional
   * panel with all triggers
   */
  public static boolean getShowTriggerPanel() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.triggerpanel.show", true);
  }

  public static void setShowTriggerPanel(boolean flag) {
    Settings.getInstance().setProperty("workbench.dbexplorer.triggerpanel.show", flag);
  }

  public static void setShowFocusInDbExplorer(boolean flag) {
    Settings.getInstance().setProperty("workbench.gui.dbobjects.showfocus", flag);
  }

  /**
   * Indicate if the column order of tables displaying meta data (table list, procedures)
   * should be saved in the workspace
   *
   * @param tableType the table for which the column order should be checked (e.g. tablelist)
   */
  public static boolean getRememberMetaColumnOrder(String tableType) {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer." + tableType + ".remember.columnorder", true);
  }

  /**
   * Control if the column order of tables displaying meta data (table list, procedures)
   * should be saved in the workspace
   *
   * @param tableType the table for which the column order should be checked (e.g. tablelist)
   * @param flag
   */
  public static void setRememberMetaColumnOrder(String tableType, boolean flag) {
    Settings.getInstance().setProperty("workbench.dbexplorer." + tableType + ".remember.columnorder", flag);
  }

  public static boolean showFocusInDbExplorer() {
    return Settings.getInstance().getBoolProperty("workbench.gui.dbobjects.showfocus", false);
  }

  public static boolean getRememberSortInDbExplorer() {
    return Settings.getInstance().getBoolProperty(PROPERTY_DBEXP_REMEMBER_SORT, false);
  }

  public static void setRememberSortInDbExplorer(boolean flag) {
    Settings.getInstance().setProperty(PROPERTY_DBEXP_REMEMBER_SORT, flag);
  }

  /**
   * Indicate if the column order in the DbExplorer's Data tab should be remembered across restarts
   */
  public static boolean getRememberColumnOrder() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.remember.columnorder", false);
  }

  /**
   * Set if the column order in the DbExplorer's Data tab should be remembered across restarts
   */
  public static void setRememberColumnOrder(boolean flag) {
    Settings.getInstance().setProperty("workbench.dbexplorer.remember.columnorder", flag);
  }

  public static boolean getStoreExplorerObjectType() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.rememberObjectType", false);
  }

  public static void setStoreExplorerObjectType(boolean flag) {
    Settings.getInstance().setProperty("workbench.dbexplorer.rememberObjectType", flag);
  }

  public static boolean getSwitchCatalogInExplorer() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.switchcatalog", true);
  }

  public static boolean getSelectDataPanelAfterRetrieve() {
    return Settings.getInstance().getBoolProperty("workbench.gui.dbobjects.autoselectdatapanel", true);
  }

  public static void setSelectDataPanelAfterRetrieve(boolean flag) {
    Settings.getInstance().setProperty("workbench.gui.dbobjects.autoselectdatapanel", flag);
  }

  public static boolean getSelectSourcePanelAfterRetrieve() {
    return Settings.getInstance().getBoolProperty("workbench.gui.dbobjects.autoselectsrcpanel", true);
  }

  public static void setSelectSourcePanelAfterRetrieve(boolean flag) {
    Settings.getInstance().setProperty("workbench.gui.dbobjects.autoselectsrcpanel", flag);
  }

  public static boolean getAutoRetrieveFKTree() {
    return Settings.getInstance().getBoolProperty("workbench.dbexplorer.fktree.autoload", true);
  }

  public static void setAutoRetrieveFKTree(boolean flag) {
    Settings.getInstance().setProperty("workbench.dbexplorer.fktree.autoload", flag);
  }

}
