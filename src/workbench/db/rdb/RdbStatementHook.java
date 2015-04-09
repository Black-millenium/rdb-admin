package workbench.db.rdb;

import workbench.db.ConnectionMgr;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.sql.StatementHook;
import workbench.sql.StatementRunner;
import workbench.sql.StatementRunnerResult;
import workbench.util.CollectionUtil;
import workbench.util.SqlUtil;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.Set;

public class RdbStatementHook
    implements StatementHook {
  public static final String SESS_ATTR_SHOWPLAN = "fb_showplan";
  public static final String SESS_ATTR_PLAN_ONLY = "fb_planonly";

  private final Object lock = new Object();

  private boolean planOnly;
  private boolean showPlan;
  private Method getPlan;
  private boolean useDefaultClassloader;

  private Set<String> explainable = CollectionUtil.caseInsensitiveSet("with", "select", "update", "delete", "insert");
  private String toExplain;

  public RdbStatementHook(WbConnection connection) {
    initialize(connection);
  }

  @Override
  public String preExec(StatementRunner runner, String sql) {
    showPlan = runner.getBoolSessionAttribute(SESS_ATTR_SHOWPLAN);
    planOnly = runner.getBoolSessionAttribute(SESS_ATTR_PLAN_ONLY);
    if (planOnly) {
      toExplain = sql;
      return null;
    }
    return sql;
  }

  @Override
  public boolean isPending() {
    return (showPlan || planOnly);
  }

  @Override
  public void postExec(StatementRunner runner, String sql, StatementRunnerResult result) {
    if (showPlan || planOnly) {
      String plan = getExecutionPlan(runner.getConnection(), sql == null ? toExplain : sql);
      if (plan != null) {
        result.addMessage("Execution plan:");
        result.addMessage("---------------");
        result.addMessage(plan.trim());
        result.addMessage("\n-- end of execution plan ---");
      }
    }
  }

  private String getExecutionPlan(WbConnection connection, String sql) {
    String verb = connection.getParsingUtil().getSqlVerb(sql);
    if (!explainable.contains(verb)) return null;

    if (getPlan == null) return null;

    String executionPlan = null;
    PreparedStatement pstmt = null;
    try {
      pstmt = connection.getSqlConnection().prepareStatement(sql);
      executionPlan = (String) getPlan.invoke(pstmt, (Object[]) null);
    } catch (Exception ex) {
      executionPlan = null;
      LogMgr.logError("RdbStatementHook.getExecutionPlan()", "Could not retrieve execution plan", ex);
    } finally {
      SqlUtil.closeStatement(pstmt);
    }
    return executionPlan;
  }

  @Override
  public boolean displayResults() {
    return !planOnly;
  }

  @Override
  public boolean fetchResults() {
    return !planOnly;
  }

  @Override
  public void close(WbConnection conn) {
  }

  private void initialize(WbConnection connection) {
    synchronized (lock) {
      try {
        Class pstmtClass = null;
        if (useDefaultClassloader) {
          pstmtClass = Class.forName("org.firebirdsql.jdbc.FirebirdPreparedStatement");
        } else {
          pstmtClass = ConnectionMgr.getInstance().loadClassFromDriverLib(connection.getProfile(), "org.firebirdsql.jdbc.FirebirdPreparedStatement");
        }

        getPlan = pstmtClass.getMethod("getExecutionPlan", (Class[]) null);
      } catch (Throwable t) {
        LogMgr.logError("RdbStatementHook.initialize()", "Could not obtain getExecutionPlan method", t);
        getPlan = null;
      }
    }
  }

  public void setUseDefaultClassloader(boolean flag) {
    useDefaultClassloader = flag;
  }
}


