/*
 * OracleViewReader.java
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
package workbench.db.oracle;

import workbench.db.*;

import java.sql.SQLException;

/**
 * @author Thomas Kellerer
 */
public class OracleViewReader
    extends DefaultViewReader {

  public OracleViewReader(WbConnection con) {
    super(con);
  }

  @Override
  public CharSequence getExtendedViewSource(TableDefinition view, boolean includeDrop, boolean includeCommit)
      throws SQLException {
    String type = view.getTable().getType();
    if (DbMetadata.MVIEW_NAME.equals(type)) {
      OracleMViewReader reader = new OracleMViewReader();
      CharSequence sql = reader.getMViewSource(this.connection, view, null, includeDrop, true);
      return sql;
    }
    return super.getExtendedViewSource(view, includeDrop, includeCommit);
  }

  @Override
  public CharSequence getViewSource(TableIdentifier viewId)
      throws NoConfigException {
    if (DbMetadata.MVIEW_NAME.equalsIgnoreCase(viewId.getType())) {
      OracleMViewReader reader = new OracleMViewReader();
      CharSequence sql = reader.getMViewSource(this.connection, new TableDefinition(viewId), null, false, false);
      return sql;
    }
    return super.getViewSource(viewId);
  }

}
