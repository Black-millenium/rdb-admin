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

package workbench.db.postgres;

import workbench.WbTestCase;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class PgRangeTypeTest
	extends WbTestCase
{

	public PgRangeTypeTest()
	{
	}

	@Test
	public void testGetDrop()
	{
		PgRangeType range = new PgRangeType("public", "timerange");
		String result = range.getObjectNameForDrop(null);
		assertEquals("public.timerange", result);
		
		String drop = range.getDropStatement(null, false);
		assertEquals("DROP TYPE public.timerange", drop);
		drop = range.getDropStatement(null, true);
		assertEquals("DROP TYPE public.timerange CASCADE", drop);
	}

}
