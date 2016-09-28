/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.link;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;


/**
 * Test autolinks (href="...")
 * 
 * @author Juergen Donnerstag
 */
public class AutolinkTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_1() throws Exception
	{
		tester.getApplication().getMarkupSettings().setAutomaticLinking(true);
		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("$up$");
		executeTest(AutolinkPage_1.class, "AutolinkPageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2() throws Exception
	{
		tester.getApplication().getMarkupSettings().setAutomaticLinking(true);
		tester.getApplication().getMarkupSettings().setStripWicketTags(true);
		executeTest(AutolinkPage_2.class, "AutolinkPageExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_3() throws Exception
	{
		tester.getApplication().getMarkupSettings().setAutomaticLinking(true);
		executeTest(AutolinkPage_3.class, "AutolinkPageExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_4() throws Exception
	{
		tester.getApplication().getMarkupSettings().setAutomaticLinking(true);
		executeTest(AutolinkPage_4.class, "AutolinkPageExpectedResult_4.html");
	}
}
