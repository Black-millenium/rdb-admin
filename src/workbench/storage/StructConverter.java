/*
 * StructConverter.java
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
package workbench.storage;

import workbench.util.SqlUtil;

import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class to create a readable display for java.sql.Struct objects
 * retrieved from the database.
 * <br/>
 * <br/>
 * This is a singleton to avoid excessive object creation during data retrieval.
 * <br/>
 * Currently this will only be used when retrieving data from an Oracle database.
 * The Postgres JDBC driver returns a String representation of "structured" types
 * (and not a Struct).
 * DB2 needs a conversion function that will be called by DB2 during retrieval
 * and will thus return a String object as well.
 *
 * @author Thomas Kellerer
 * @see RowDataReader#read(java.sql.ResultSet, boolean)
 */
public class StructConverter {

  private final SimpleDateFormat timestampFormatter;
  private final SimpleDateFormat dateFormatter;
  private final SimpleDateFormat timeFormatter;
  private final DecimalFormat numberFormatter;
  private StructConverter() {
    // The ANSI literals should be OK, as all databases that support structs
    // also support ANSI compliant date literals.
    timestampFormatter = new SimpleDateFormat("'TIMESTAMP '''yyyy-MM-dd HH:mm:ss''");
    timeFormatter = new SimpleDateFormat("'TIME '''HH:mm:ss''");
    dateFormatter = new SimpleDateFormat("'DATE '''yyyy-MM-dd''");
    DecimalFormatSymbols symb = new DecimalFormatSymbols();
    symb.setDecimalSeparator('.');
    numberFormatter = new DecimalFormat("0.#", symb);
    numberFormatter.setMinimumIntegerDigits(0);
    numberFormatter.setMaximumFractionDigits(100);
  }

  public static StructConverter getInstance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Create a display for the given Struct.
   * <br>
   * The display closeley duplicates the way SQL*Plus shows object types.
   * If attributes of the Struct are itself a Struct, this method is called
   * recursively.
   * <br/>
   * The name of the Struct will be followed by all values in paranthesis, e.g.
   * <tt>MY_TYPE('Hello', 'World', 42)</tt>
   * <br/>
   * Note that Oracle apparently always returns the owner as part of the type name,
   * so the actual display will be <tt>SCOTT.MY_TYPE('Hello', 'World', 42)</tt>
   *
   * @param data the Struct to convert
   * @return a String representation of the data
   * @throws SQLException
   */
  public String getStructDisplay(Struct data)
      throws SQLException {
    if (data == null) return null;

    Object[] attr = data.getAttributes();
    if (attr == null) return null;

    StringBuilder buffer = new StringBuilder(attr.length * 20);

    String name = data.getSQLTypeName();
    if (name != null) buffer.append(name);

    buffer.append('(');
    boolean first = true;
    for (Object a : attr) {
      if (!first) buffer.append(", ");
      else first = false;
      if (a == null) {
        buffer.append("NULL");
      } else {
        if (a instanceof Struct) {
          buffer.append(getStructDisplay((Struct) a));
        } else {
          appendValue(buffer, a);
        }
      }
    }
    buffer.append(')');
    return buffer.toString();
  }

  public void appendValue(StringBuilder buffer, Object a) {
    if (a instanceof CharSequence) {
      // String need to be enclosed in single quotes
      buffer.append('\'');
      buffer.append(SqlUtil.escapeQuotes(a.toString()));
      buffer.append('\'');
    } else if (a instanceof Timestamp) {
      synchronized (timestampFormatter) {
        buffer.append(timestampFormatter.format((Timestamp) a));
      }
    } else if (a instanceof Time) {
      synchronized (timeFormatter) {
        buffer.append(timeFormatter.format((Time) a));
      }
    } else if (a instanceof Date) {
      synchronized (dateFormatter) {
        buffer.append(dateFormatter.format((Date) a));
      }
    } else if (a instanceof Number) {
      synchronized (numberFormatter) {
        buffer.append(numberFormatter.format(a));
      }
    } else {
      // for anything else, rely on the driver
      // as the JDBC type of this attribute is not known, we also
      // cannot dispatch this to a DataConverter
      buffer.append(a.toString());
    }
  }

  protected static class InstanceHolder {
    protected static final StructConverter INSTANCE = new StructConverter();
  }
}
