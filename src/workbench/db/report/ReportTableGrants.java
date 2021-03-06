/*
 * ReportTableGrants.java
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
package workbench.db.report;

import workbench.db.TableGrant;
import workbench.db.TableGrantReader;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;

import java.util.Collection;
import java.util.Collections;


/**
 * Generate XML report information about table grants.
 *
 * @author Thomas Kellerer
 * @see workbench.db.TableGrantReader#getTableGrants(workbench.db.WbConnection, workbench.db.TableIdentifier)
 */
public class ReportTableGrants {
  public static final String TAG_GRANT = "grant";
  public static final String TAG_GRANT_GRANTEE = "grantee";
  public static final String TAG_GRANT_PRIV = "privilege";
  public static final String TAG_GRANT_GRANTABLE = "grantable";
  private Collection<TableGrant> grants;

  public ReportTableGrants(WbConnection con, TableIdentifier tbl) {
    TableGrantReader reader = new TableGrantReader();
    grants = reader.getTableGrants(con, tbl);
  }

  public ReportTableGrants(Collection<TableGrant> tableGrants) {
    this.grants = tableGrants;
  }

  public void appendXml(StringBuilder result, StringBuilder indent) {
    if (grants.isEmpty()) return;

    TagWriter tagWriter = new TagWriter();

    StringBuilder indent1 = new StringBuilder(indent);
    indent1.append("  ");

    for (TableGrant grant : grants) {
      tagWriter.appendOpenTag(result, indent, TAG_GRANT);
      result.append('\n');
      tagWriter.appendTag(result, indent1, TAG_GRANT_PRIV, grant.getPrivilege());
      tagWriter.appendTag(result, indent1, TAG_GRANT_GRANTEE, grant.getGrantee());
      tagWriter.appendTag(result, indent1, TAG_GRANT_GRANTABLE, grant.isGrantable());
      tagWriter.appendCloseTag(result, indent, TAG_GRANT);
    }
  }

  public Collection<TableGrant> getGrants() {
    return Collections.unmodifiableCollection(grants);
  }

}

