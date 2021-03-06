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
package workbench.util;

import workbench.sql.parser.ParserType;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class SqlParsingUtilTest
{

	public SqlParsingUtilTest()
	{
	}

	@Test
	public void testGetSqlVerb()
	{
		SqlParsingUtil util = new SqlParsingUtil(ParserType.Standard);
		String result = util.getSqlVerb("-- foobar\nselect 42");
		assertEquals("SELECT", result);
	}

	@Test
	public void testStripVerb()
	{
		String sql = "-- foobar\nselect 42";
		SqlParsingUtil util = new SqlParsingUtil(ParserType.Standard);
		assertEquals("42", util.stripVerb(sql));

		sql = "-- foobar\n /* bla */ select 42";
		assertEquals("42", util.stripVerb(sql));
	}

	@Test
	public void testGetPos()
	{
		String sql = "select /* from */ some_column from some_table join other_table using (x)";
		SqlParsingUtil util = new SqlParsingUtil(ParserType.Standard);
		int pos = util.getKeywordPosition("FROM", sql);
		assertEquals(30, pos);
		String fromPart = util.getFromPart(sql);
		assertEquals(" some_table join other_table using (x)", fromPart);

		sql = "select /* from */ \n" +
			"some_column, \n" +
			" -- from blabla \n" +
			" from some_table join other_table using (x)";
		fromPart = util.getFromPart(sql);
		assertEquals(" some_table join other_table using (x)", fromPart);
	}
}
