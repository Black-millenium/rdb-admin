/*
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
package workbench.db.ibm;

import workbench.db.DefaultDataTypeResolver;

import java.sql.Types;

/**
 * @author Thomas Kellerer
 */
public class InformixDataTypeResolver
    extends DefaultDataTypeResolver {

  @Override
  public int fixColumnType(int type, String dbmsType) {
    if (type == Types.CHAR && "INTERVAL".equalsIgnoreCase(dbmsType)) {
      // Don't treat intervals as CHAR.
      // As JDBC does not have a Type "INTERVAL" this should at least be "OTHER"
      return Types.OTHER;
    }
    return type;
  }

  /**
   * Handles Informix varchar(m,r) correctly.
   *
   * @see workbench.util.SqlUtil#getSqlTypeDisplay(java.lang.String, int, int, int)
   */
  @Override
  public String getSqlTypeDisplay(String dbmsName, int sqlType, int size, int digits) {
    if (sqlType == Types.VARCHAR && size > 255) {
      String display = dbmsName;

      int charLength = size % 256;
      display += "(" + Integer.toString(charLength);

      int reserved = (int) (2810 / 256);
      if (reserved > 0) {
        display += "," + Integer.toString(reserved);
      }
      display += ")";
      return display;
    } else if (sqlType == Types.LONGVARCHAR) {
      if ("longvarchar".equalsIgnoreCase(dbmsName)) {
        return dbmsName + "(" + Integer.toString(size) + ")";
      } else {
        return dbmsName;
      }
    }
    return super.getSqlTypeDisplay(dbmsName, sqlType, size, digits);
  }

}
