package workbench.db.rdb;

import workbench.db.ViewGrantReader;

public class RdbViewGrantReader
    extends ViewGrantReader {

  @Override
  public String getViewGrantSql() {
    String sql = "select trim(p.rdb$user) as grantee,  \n" +
        "case p.rdb$privilege \n" +
        "  when 'S' then 'SELECT'  \n" +
        "  when 'U' then 'UPDATE'  \n" +
        "  when 'D' then 'DELETE'  \n" +
        "  when 'I' then 'INSERT' \n" +
        "end as privilege,  \n" +
        "case p.rdb$grant_option  \n" +
        "  when 1 then 'YES' \n" +
        "  else 'NO' \n" +
        "end as is_grantable \n" +
        "from RDB$USER_PRIVILEGES p, rdb$relations r  \n" +
        "WHERE p.rdb$relation_name = r.rdb$relation_name \n" +
        "and p.rdb$user <> r.rdb$owner_name \n" +
        "AND  p.rdb$relation_name = ? ";

    return sql;
  }

  @Override
  public int getIndexForTableNameParameter() {
    return 1;
  }

}
