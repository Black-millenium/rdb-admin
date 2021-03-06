/*
 * TypeMapper.java
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
package workbench.db;

import workbench.log.LogMgr;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to map datatypes from JDBC types to DBMS types.
 *
 * @author Thomas Kellerer
 */
public class TypeMapper {
  public static final String PLACEHOLDER_DIGITS = "$digits";
  public static final String PLACEHOLDER_SIZE = "$size";

  private WbConnection targetDb;
  private Map<Integer, String> typeInfo;
  private Map<Integer, String> userMapping;

  /**
   * For testing purposes only!
   */
  TypeMapper() {

  }

  /**
   * Create a TypeMapper for the DBMS specified through the target connection.
   *
   * @param targetConnection
   */
  public TypeMapper(WbConnection targetConnection) {
    this.targetDb = targetConnection;
    this.createTypeMap();
  }

  private String findAlternateBlobType(int type) {
    List<Integer> allBlobTypes = new ArrayList<Integer>(4);

    // BLOBs are reported as BLOB, VARBINARY (Postgres), LONGVARBNARY (HSQL) and
    // possibly as BINARY. So we need to test each of them. The order here
    // is a personal feeling which type should be preferred over others ;)
    allBlobTypes.add(Integer.valueOf(Types.BLOB));
    allBlobTypes.add(Integer.valueOf(Types.LONGVARBINARY));
    allBlobTypes.add(Integer.valueOf(Types.VARBINARY));
    allBlobTypes.add(Integer.valueOf(Types.BINARY));

    for (Integer blobType : allBlobTypes) {
      // we are looking for an alternative to the passed type
      // so ignore that one
      if (blobType == type) continue;

      String name = typeInfo.get(blobType);
      if (name != null) return name;
    }
    return null;
  }

  private String findAlternateClobType(int type) {
    List<Integer> allClobTypes = new ArrayList<Integer>(4);

    // CLOBs are reported as CLOB, LONGVARCHAR
    // The order here is a personal feeling which type should be preferred over others ;)
    allClobTypes.add(Integer.valueOf(Types.CLOB));
    allClobTypes.add(Integer.valueOf(Types.LONGVARCHAR));
    allClobTypes.add(Integer.valueOf(Types.NCLOB));
    allClobTypes.add(Integer.valueOf(Types.LONGNVARCHAR));


    for (Integer clobType : allClobTypes) {
      // we are looking for an alternative to the passed type
      // so ignore that one
      if (clobType == type) continue;

      String name = typeInfo.get(clobType);
      if (name != null) return name;
    }
    return null;
  }

  /**
   * Returns a parsed version of the user type-mapping if available.
   * <p/>
   * If a user type-mapping is available for the given type, this will
   * be returned, with the placeholders <tt>$size</tt> and <tt>$digits</tt>
   * replaced in the type definition.
   * <p/>
   * Unlike the built-in types, the placeholders <tt>$size</tt> and <tt>$digits</tt> are required
   * in the definition.
   *
   * @param type
   * @param size
   * @param digits
   * @return a valid data type definition for the DBMS
   */
  protected String getUserMapping(int type, int size, int digits) {
    if (userMapping == null) return null;
    Integer key = Integer.valueOf(type);
    String userType = userMapping.get(key);
    if (userType == null) return null;

    userType = userType.replace(PLACEHOLDER_SIZE, Integer.toString(size));
    userType = userType.replace(PLACEHOLDER_DIGITS, Integer.toString(digits));

    return userType;
  }

  public String getTypeName(int type, int size, int digits) {
    String userType = getUserMapping(type, size, digits);
    if (userType != null) return userType;

    Integer key = Integer.valueOf(type);
    String name = this.typeInfo.get(key);

    // BLOBs and CLOBs are mapped to different types in different
    // DBMS and not all are using the same java.sql.Types value.
    // This code tries to find a mapping even if the "desired" input
    // type was not part of the type info returned by the driver
    if (name == null) {
      if (SqlUtil.isBlobType(type)) {
        name = findAlternateBlobType(type);
      } else if (SqlUtil.isClobType(type)) {
        name = findAlternateClobType(type);
      }
      if (name != null) {
        LogMgr.logInfo("TypeMapper.getTypeName()", "Could not find a direct mapping for java.sql.Types." + SqlUtil.getTypeName(type) + ", using DBMS type: " + name);
      }
    }

    if (name == null) {
      return SqlUtil.getTypeName(type);
    }

    return this.targetDb.getMetadata().getDataTypeResolver().getSqlTypeDisplay(name, type, size, digits);
  }

  /**
   * Parse and apply a user-defined type mapping.
   * <p/>
   * The mapping string has the format:
   * Mapping pairs are delimited with a colon, e.g. 12:varchar (maps Types.VARCHAR to "varchar")
   * <br/>
   * Multiple pairs are delimited with a semicolon, e.g:<br/>
   * <tt>3:DOUBLE;2:NUMERIC($size, $digits);-1:VARCHAR2($size);93:DATETIME YEAR TO SECOND</tt><br/>
   * <br/>
   * The placeholders <tt>$size</tt> and <tt>$digits</tt> are replaced with the approriate values when
   * the data type is used
   *
   * @param mapping
   * @see #getTypeName(int, int, int)
   */
  public void parseUserTypeMap(String mapping) {
    userMapping = new HashMap<Integer, String>();

    if (StringUtil.isBlank(mapping)) return;
    List<String> types = StringUtil.stringToList(mapping, ";", true, true, false, false);

    for (String type : types) {
      String[] def = type.split(":");
      if (def != null && def.length == 2) {
        try {
          Integer typeValue = Integer.parseInt(def[0]);
          String old = this.userMapping.put(typeValue, def[1]);
          LogMgr.logInfo("TypeMapp.parseUserTypeMap()", "Mapping java.sql.Types." + SqlUtil.getTypeName(typeValue) + " to usertype: [" + def[1] + "] overwriting mapping to [" + old == null ? "" : old + "]");
        } catch (Exception e) {
          LogMgr.logError("TypeMapp.parseUserTypeMap()", "Could not parse entry: " + type, e);
        }
      }
    }
  }

  /**
   * Builds up the mapping from JDBC type values (integers) to DBMS data types as reported by the driver.
   * The getTypeInfo() from the driver is intended to be used in the different direction (from a DBMS type to a JDBC type)
   * and thus a single JDBC type can map to multiple different DBMS types (especially STRUCT and OTHER).
   * <br/>
   * The TypeMapper always uses the first match that is returned from the driver and ignores all others.
   * To overwrite this behaviour, create a user mapping in workbench.settings.
   * <br>
   *
   * @see #parseUserTypeMap(java.lang.String)
   */
  private void createTypeMap() {
    ResultSet rs = null;
    this.typeInfo = new HashMap<Integer, String>(27);
    boolean cleanup = targetDb.getDbSettings().cleanupTypeMappingNames();

    Pattern p = Pattern.compile("[\\(\\)\\[\\]{}'\"`]");

    try {
      LogMgr.logInfo("TypeMapper.createTypeMap()", "Reading type map for " + targetDb.getDbId());
      rs = this.targetDb.getSqlConnection().getMetaData().getTypeInfo();
      while (rs.next()) {
        String name = rs.getString(1);
        int type = rs.getInt(2);

        // we can't handle arrays anyway
        if (type == java.sql.Types.ARRAY || type == java.sql.Types.OTHER) continue;

        if (cleanup) {
          Matcher m = p.matcher(name);
          String clean = m.replaceAll("");
          if (!clean.equals(name)) {
            LogMgr.logInfo("TypeMapper.createTypeMap()", "Using type name [" + clean + "] instead of [" + name + "]");
            name = clean;
          }
        }

        Integer key = Integer.valueOf(type);
        if (this.typeInfo.containsKey(key)) {
          LogMgr.logWarning("TypeMapper.createTypeMap()", "The mapping from java.sql.Types." + SqlUtil.getTypeName(type) + " to  DBMS type [" + name + "] will be ignored. A mapping is already present.");
        } else {
          LogMgr.logInfo("TypeMapper.createTypeMap()", "Mapping java.sql.Types." + SqlUtil.getTypeName(type) + " to DBMS type [" + name + "] ");
          this.typeInfo.put(key, name);
        }
      }
    } catch (SQLException e) {
      LogMgr.logError("TypeMapper.createTypeMap()", "Error reading type info for target connection", e);
      this.typeInfo = new HashMap<Integer, String>();
    } finally {
      SqlUtil.closeResult(rs);
    }
    parseUserTypeMap(targetDb.getDbSettings().getJDBCTypeMapping());
  }

}

