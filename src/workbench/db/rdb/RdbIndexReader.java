package workbench.db.rdb;

import workbench.db.*;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.util.SqlUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RdbIndexReader
    extends JdbcIndexReader {
  // This is the basic statement from the Jaybird driver, enhanced to support
  // function based indexes.
  private static final String GET_INDEX_INFO =
      "SELECT NULL as TABLE_CAT, \n" +
          "       NULL as TABLE_SCHEM, \n" +
          "       trim(ind.RDB$RELATION_NAME) AS TABLE_NAME, \n" +
          "       case  \n" +
          "           when ind.RDB$UNIQUE_FLAG is null then 1  \n" +
          "           when ind.RDB$UNIQUE_FLAG = 1 then 0 \n" +
          "           else 1 \n" +
          "        end AS NON_UNIQUE, \n" +
          "       NULL as INDEX_QUALIFIER, \n" +
          "       trim(ind.RDB$INDEX_NAME) as INDEX_NAME, \n" +
          "       NULL as \"TYPE\", \n" +
          "       coalesce(ise.rdb$field_position,0) +1 as ORDINAL_POSITION, \n" +
          "       trim(coalesce(ise.rdb$field_name, 'COMPUTED BY '|| ind.rdb$expression_source)) as COLUMN_NAME, \n" +
          "       case \n" +
          "           when ind.RDB$INDEX_TYPE = 1 then 'D'  \n" +
          "           else 'A' \n" +
          "        end as ASC_OR_DESC, " +
          "       0 as CARDINALITY, " +
          "       0 as \"PAGES\", \n" +
          "       null as FILTER_CONDITION, \n" +
          "       ind.RDB$FOREIGN_KEY \n" +
          "FROM rdb$indices ind " +
          " LEFT JOIN rdb$index_segments ise ON ind.rdb$index_name = ise.rdb$index_name " +
          "WHERE ind.rdb$relation_name = ? " +
          "ORDER BY 4, 6, 8";
  private PreparedStatement indexStatement;

  public RdbIndexReader(DbMetadata meta) {
    super(meta);
  }

  @Override
  public ResultSet getIndexInfo(TableIdentifier table, boolean unique)
      throws SQLException {
    if (this.indexStatement != null) {
      LogMgr.logWarning("RdbIndexReader.getIndexInfo()", "getIndexInfo() called with pending results!");
      indexInfoProcessed();
    }
    WbConnection con = this.metaData.getWbConnection();

    if (Settings.getInstance().getDebugMetadataSql()) {
      LogMgr.logDebug("RdbIndexReader.getIndexInfo()", "Using SQL:\n " + SqlUtil.replaceParameters(GET_INDEX_INFO, table.getTableName()));
    }

    this.indexStatement = con.getSqlConnection().prepareStatement(GET_INDEX_INFO);
    this.indexStatement.setString(1, table.getRawTableName());
    ResultSet rs = this.indexStatement.executeQuery();
    return rs;
  }

  @Override
  public void processIndexList(Collection<IndexDefinition> indexList) {
    for (IndexDefinition index : indexList) {
      List<IndexColumn> columns = index.getColumns();
      String dir = null;
      String computed = null;
      for (IndexColumn col : columns) {
        if (dir == null) {
          dir = col.getDirection();
        }
        col.setDirection(null);
        if (col.getColumn().startsWith("COMPUTED") && computed == null) {
          computed = col.getColumn();
        }
      }
      index.setDirection(dir);
      index.setIndexExpression(computed);
    }
    Iterator<IndexDefinition> itr = indexList.iterator();
    while (itr.hasNext()) {
      IndexDefinition idx = itr.next();
      if (idx.isUnique() && idx.getName().startsWith("RDB$PRIM")) {
        idx.setPrimaryKeyIndex(true);
      }
    }
  }

  @Override
  protected void processIndexResultRow(ResultSet rs, IndexDefinition index, TableIdentifier tbl)
      throws SQLException {
    String pk = rs.getString("RDB$FOREIGN_KEY");
    if (pk != null) {
      index.setAutoGenerated(true);
    }
  }


  @Override
  public void indexInfoProcessed() {
    SqlUtil.closeStatement(indexStatement);
    indexStatement = null;
  }

}
