/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015 Thomas Kellerer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.gui.settings;

import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.sql.DelimiterDefinition;

import java.util.*;


/**
 * @author Thomas Kellerer
 */
public class DbDelimiter {
  private final String dbid;
  private final String displayName;
  private String delimiter;

  public DbDelimiter(String id, String name) {
    this.dbid = id;
    this.displayName = name;
  }

  public static DbDelimiter[] getMapping() {
    List<DbDelimiter> result = new ArrayList<DbDelimiter>();
    Map<String, String> map = Settings.getInstance().getDbIdMapping();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      DbDelimiter def = new DbDelimiter(entry.getKey(), entry.getValue());
      String delim = Settings.getInstance().getDbDelimiter(entry.getKey());
      def.setDelimiter(delim);
      result.add(def);
    }
    Comparator<DbDelimiter> comp = new Comparator<DbDelimiter>() {
      @Override
      public int compare(DbDelimiter o1, DbDelimiter o2) {
        return o1.displayName.compareToIgnoreCase(o2.displayName);
      }
    };
    Collections.sort(result, comp);
    DbDelimiter all = new DbDelimiter("*", ResourceMgr.getString("TxtDefault"));
    DelimiterDefinition delim = Settings.getInstance().getAlternateDelimiter(null);
    if (delim != null) {
      all.setDelimiter(delim.getDelimiter());
    }
    result.add(0, all);
    return result.toArray(new DbDelimiter[0]);
  }

  public String getDbid() {
    return dbid;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDelimiter() {
    return delimiter == null ? "" : delimiter;
  }

  public void setDelimiter(String delimiterString) {
    this.delimiter = delimiterString;
  }

  @Override
  public String toString() {
    return displayName;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + this.dbid.hashCode();
    return hash;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other instanceof DbDelimiter) {
      return this.dbid.equals(((DbDelimiter) other).dbid);
    }
    return false;
  }
}
