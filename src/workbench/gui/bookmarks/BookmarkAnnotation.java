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
package workbench.gui.bookmarks;

import workbench.resource.GuiSettings;
import workbench.sql.ResultNameParser;
import workbench.sql.UseTabAnnotation;
import workbench.sql.WbAnnotation;
import workbench.sql.lexer.SQLLexer;
import workbench.sql.lexer.SQLLexerFactory;
import workbench.sql.lexer.SQLToken;
import workbench.sql.parser.ParserType;
import workbench.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Kellerer
 */
public class BookmarkAnnotation
    extends WbAnnotation {
  public static final String ANNOTATION = "WbTag";

  private List<String> validTags = new ArrayList<String>(3);

  public BookmarkAnnotation() {
    super(ANNOTATION);
    setUseResultTag(GuiSettings.getUseResultTagForBookmarks());
  }

  public void setUseResultTag(boolean useResultTag) {
    validTags.clear();
    validTags.add(ANNOTATION.toLowerCase());
    if (useResultTag) {
      validTags.add("@" + ResultNameParser.ANNOTATION.toLowerCase());
      validTags.add("@" + UseTabAnnotation.ANNOTATION.toLowerCase());
    }
  }

  /**
   * Parses the given SQL script for bookmark annotations.
   * <p/>
   * If procedures and functions should be show as bookmarks they are added as well.
   * For that {@link ProcedureBookmarks} is used passing the tokens from the
   * SQLLexer while parsing the current script so only a single parse of the script is necessary.
   *
   * @param script the script to parse
   * @return the list of bookmarks found
   * @see GuiSettings#getParseProceduresForBookmarks()
   * @see ProcedureBookmarks
   */
  public List<NamedScriptLocation> getBookmarks(String script, String tabId, ParserType type) {
    List<NamedScriptLocation> bookmarks = new ArrayList<NamedScriptLocation>();

    ProcedureBookmarks parser = null;
    if (GuiSettings.getParseProceduresForBookmarks()) {
      parser = new ProcedureBookmarks(tabId);
    }

    SQLLexer lexer = SQLLexerFactory.createLexer(type, script);
    SQLToken token = lexer.getNextToken(true, false);

    while (token != null) {
      if (token.isComment()) {
        String locationName = findTagValue(token);
        if (locationName != null) {
          NamedScriptLocation bookmark = new NamedScriptLocation(locationName, token.getCharBegin(), tabId);
          bookmarks.add(bookmark);
        }
      }
      if (parser != null) {
        parser.processToken(token);
      }
      token = lexer.getNextToken(true, false);
    }

    if (parser != null) {
      bookmarks.addAll(parser.getBookmarks());
    }
    return bookmarks;
  }

  private String findTagValue(SQLToken token) {
    for (String tag : validTags) {
      String value = StringUtil.trim(extractAnnotationValue(token, tag));
      if (value != null) return value;
    }
    return null;
  }
}
