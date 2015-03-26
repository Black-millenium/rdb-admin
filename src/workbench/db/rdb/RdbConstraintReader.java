package workbench.db.rdb;

import workbench.db.AbstractConstraintReader;
import workbench.db.DbMetadata;

public class RdbConstraintReader
    extends AbstractConstraintReader {

  private final String TABLE_SQL =
      "select trim(cc.rdb$constraint_name), trg.rdb$trigger_source " +
          "from rdb$relation_constraints rc  \n" +
          "  join rdb$check_constraints cc on rc.rdb$constraint_name = cc.rdb$constraint_name \n" +
          "  join rdb$triggers trg on cc.rdb$trigger_name = trg.rdb$trigger_name \n" +
          "where rc.rdb$relation_name = ? \n" +
          "  and rc.rdb$constraint_type = 'CHECK' \n" +
          "  and trg.rdb$trigger_type = 1 \n";

  public RdbConstraintReader() {
    super(DbMetadata.DBID_FIREBIRD);
  }

  @Override
  public String getColumnConstraintSql() {
    return null;
  }

  @Override
  public String getTableConstraintSql() {
    return TABLE_SQL;
  }
}
