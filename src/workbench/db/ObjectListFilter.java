/*
 * ObjectListFilter.java
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

import workbench.resource.Settings;
import workbench.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Kellerer
 */
public class ObjectListFilter {
  private Map<String, ObjectNameFilter> filterMap = new HashMap<String, ObjectNameFilter>();

  public ObjectListFilter(String dbid) {
    String synRegex = Settings.getInstance().getProperty("workbench.db." + dbid + ".exclude.synonyms", null);
    addFilter(synRegex, "SYNONYM");
    ObjectNameFilter f = filterMap.get("SYNONYM");
    if (f != null) {
      filterMap.put("ALIAS", f);
    }

    String tableRegex = Settings.getInstance().getProperty("workbench.db." + dbid + ".exclude.tables", null);
    addFilter(tableRegex, "TABLE");

    String viewRegex = Settings.getInstance().getProperty("workbench.db." + dbid + ".exclude.views", null);
    addFilter(viewRegex, "VIEW");
  }

  private void addFilter(String regex, String type) {
    if (StringUtil.isNonBlank(regex) && StringUtil.isNonBlank(type)) {
      ObjectNameFilter filter = new ObjectNameFilter();
      filter.setExpressionList(regex);
      filterMap.put(type, filter);
    }
  }

  public boolean isExcluded(String objectType, String objectName) {
    ObjectNameFilter filter = filterMap.get(objectType);
    if (filter == null) return false;
    return filter.isExcluded(objectName);
  }

}
