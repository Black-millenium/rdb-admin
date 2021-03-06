/*
 * CaseInsensitiveComparator.java
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
package workbench.util;

import workbench.db.objectcache.DbObjectCacheFactory;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A case-insensitive Comparator for String which
 * can handle null values as well (as opposed to String.CASE_INSENSITIVE_ORDER)
 *
 * @author Thomas Kellerer
 */
public class CaseInsensitiveComparator
    implements Comparator<String>, Serializable {
  public static final CaseInsensitiveComparator INSTANCE = new CaseInsensitiveComparator();
  private static final long serialVersionUID = DbObjectCacheFactory.CACHE_VERSION_UID;

  private boolean ignoreQuotes;

  public CaseInsensitiveComparator() {
  }

  public void setIgnoreQuotes(boolean flag) {
    this.ignoreQuotes = flag;
  }

  /**
   * Compares to two strings.
   * null values are "sorted" after non-null values.
   * i.e. compare(null, "something") returns -1
   * and compare("something", null) returns 1
   *
   * @param value1 the first String, maybe null
   * @param value2 the second String, maybe null
   * @return 0 if both are null or compareToIgnoreCase() returns 0
   * @see workbench.util.StringUtil#compareStrings(String, String, boolean)
   */
  @Override
  public int compare(String value1, String value2) {
    if (value1 == null && value2 == null) return 0;
    if (value1 == null) return -1;
    if (value2 == null) return 1;
    if (ignoreQuotes) {
      return StringUtil.trimQuotes(value1).compareToIgnoreCase(StringUtil.trimQuotes(value2));
    }
    return value1.compareToIgnoreCase(value2);
  }
}


