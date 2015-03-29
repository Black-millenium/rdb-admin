package workbench.db.rdb;

import workbench.db.JdbcProcedureReader;
import workbench.db.ProcedureReader;
import workbench.db.WbConnection;
import workbench.storage.DataStore;
import workbench.util.StringUtil;

public class RdbProcedureReader
    extends JdbcProcedureReader {
  public RdbProcedureReader(WbConnection conn) {
    super(conn);
  }

  @Override
  public boolean needsHeader(CharSequence procedureBody) {
    String packageHeader = "CREATE PACKAGE";
    if (procedureBody.subSequence(0, packageHeader.length()).equals(packageHeader)) {
      return false;
    }
    return true;
  }

  @Override
  public StringBuilder getProcedureHeader(String aCatalog, String aSchema, String aProcname, int procType) {
    StringBuilder source = new StringBuilder();
    try {
      DataStore ds = this.getProcedureColumns(aCatalog, aSchema, aProcname, null);
      source.append("CREATE PROCEDURE ");
      source.append(aProcname);
      String retType = null;
      int count = ds.getRowCount();
      int added = 0;
      for (int i = 0; i < count; i++) {
        String vartype = ds.getValueAsString(i, ProcedureReader.COLUMN_IDX_PROC_COLUMNS_DATA_TYPE);
        String name = ds.getValueAsString(i, ProcedureReader.COLUMN_IDX_PROC_COLUMNS_COL_NAME);
        String ret = ds.getValueAsString(i, ProcedureReader.COLUMN_IDX_PROC_COLUMNS_RESULT_TYPE);
        if ("OUT".equals(ret)) {
          retType = "(" + name + " " + vartype + ")";
        } else {
          if (added > 0) {
            source.append(',');
          } else {
            source.append(" (");
          }
          source.append(name);
          source.append(' ');
          source.append(vartype);
          added++;
        }
      }
      if (added > 0) source.append(')');
      if (retType != null) {
        source.append("\nRETURNS ");
        source.append(retType);
      }
      source.append("\nAS\n");
    } catch (Exception e) {
      source = StringUtil.emptyBuilder();
    }
    return source;
  }

}
