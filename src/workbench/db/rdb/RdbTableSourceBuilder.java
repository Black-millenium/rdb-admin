package workbench.db.rdb;

import workbench.db.ColumnIdentifier;
import workbench.db.TableIdentifier;
import workbench.db.TableSourceBuilder;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class RdbTableSourceBuilder
    extends TableSourceBuilder {

  public RdbTableSourceBuilder(WbConnection con) {
    super(con);
  }

  @Override
  public void readTableOptions(TableIdentifier tbl, List<ColumnIdentifier> columns) {
    if (tbl.getSourceOptions().isInitialized()) return;

    String sql =
        "select trim(t.rdb$type_name) \n" +
            "from rdb$relations r \n" +
            "  join rdb$types t on r.rdb$relation_type = t.rdb$type and t.rdb$field_name = 'RDB$RELATION_TYPE' \n" +
            "where coalesce (r.rdb$system_flag, 0) = 0 \n" +
            "  and rdb$relation_name = ? ";

    PreparedStatement stmt = null;
    ResultSet rs = null;

    StringBuilder options = new StringBuilder(50);
    try {
      stmt = dbConnection.getSqlConnection().prepareStatement(sql);
      stmt.setString(1, tbl.getTableName());
      rs = stmt.executeQuery();
      if (rs.next()) {
        String type = rs.getString(1);
        if (StringUtil.equalStringIgnoreCase(type, "GLOBAL_TEMPORARY_PRESERVE")) {
          tbl.getSourceOptions().setTypeModifier("GLOBAL TEMPORARY");
          options.append("ON COMMIT PRESERVE ROWS");
          tbl.getSourceOptions().addConfigSetting("on_commit", "preserve");
        } else if (StringUtil.equalStringIgnoreCase(type, "GLOBAL_TEMPORARY_DELETE")) {
          tbl.getSourceOptions().setTypeModifier("GLOBAL TEMPORARY");
          options.append("ON COMMIT DELETE ROWS");
          tbl.getSourceOptions().addConfigSetting("on_commit", "delete");
        }
      }
      tbl.getSourceOptions().setTableOption(options.toString());
      tbl.getSourceOptions().setInitialized();
    } catch (Exception ex) {
      LogMgr.logError("RdbTableSourceBuilder.readTableOptions()", "Could not read table options using query:\n" + sql, ex);
    } finally {
      SqlUtil.closeAll(rs, stmt);
    }

  }
}
