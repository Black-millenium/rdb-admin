/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015, Thomas Kellerer.
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

package workbench.db.report;

import workbench.db.BaseObjectType;
import workbench.db.ColumnIdentifier;
import workbench.util.SqlUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Kellerer
 */
public class ReportObjectType {
  public static final String TAG_TYPE_DEF = "type-def";
  public static final String TAG_TYPE_NAME = "type-name";
  public static final String TAG_TYPE_CATALOG = "type-catalog";
  public static final String TAG_TYPE_SCHEMA = "type-schema";
  public static final String TAG_TYPE_COMMENT = "type-comment";
  private final TagWriter tagWriter = new TagWriter();
  private BaseObjectType type;
  private String schemaToUse;

  public ReportObjectType(BaseObjectType objectType) {
    this.type = objectType;
  }

  public void writeXml(Writer out)
      throws IOException {
    StringBuilder line = this.getXml();
    out.append(line);
  }

  public BaseObjectType getType() {
    return this.type;
  }

  public void setSchemaToUse(String schemaToUse) {
    this.schemaToUse = schemaToUse;
  }

  public StringBuilder getXml() {
    return getXml(TAG_TYPE_DEF, new StringBuilder("  "));
  }

  @Override
  public String toString() {
    return this.type.toString();
  }

  /**
   * Return an XML representation of this type information.
   * <p/>
   * The columns will be listed alphabetically not in the order
   * they were retrieved from the database.
   */
  public StringBuilder getXml(String tag, StringBuilder indent) {
    StringBuilder line = new StringBuilder(500);
    StringBuilder myIndent = new StringBuilder(indent);
    myIndent.append("  ");

    tagWriter.appendOpenTag(line, indent, tag, "name", SqlUtil.removeObjectQuotes(type.getObjectName()));
    line.append('\n');
    appendDefinitionXml(line, myIndent);

    List<ColumnIdentifier> atts = new ArrayList<ColumnIdentifier>(type.getAttributes());
    if (atts.size() > 0) {
      Collections.sort(atts);
      for (ColumnIdentifier col : atts) {
        ReportColumn rc = new ReportColumn(col);
        rc.appendXml(line, myIndent);
      }
    }
    tagWriter.appendCloseTag(line, indent, tag);
    line.append('\n');

    return line;
  }

  public void appendDefinitionXml(StringBuilder line, StringBuilder myIndent) {
    tagWriter.appendTag(line, myIndent, TAG_TYPE_CATALOG, SqlUtil.removeObjectQuotes(this.type.getCatalog()));
    tagWriter.appendTag(line, myIndent, TAG_TYPE_SCHEMA, schemaToUse == null ? SqlUtil.removeObjectQuotes(this.type.getSchema()) : schemaToUse);
    tagWriter.appendTag(line, myIndent, TAG_TYPE_NAME, SqlUtil.removeObjectQuotes(this.type.getObjectName()));
    tagWriter.appendTag(line, myIndent, TAG_TYPE_COMMENT, type.getComment());
  }

}
