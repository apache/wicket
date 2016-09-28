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
 * Simple tester that demonstrates the mock http application code (and checks that it is working)
 * 
 * @author Chris Turner
 */
public class HrefTest extends WicketTestCase
{

	/**
	 * Simple Label
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_1() throws Exception
	{
		tester.getApplication().getMarkupSettings().setStripWicketTags(false);
		executeTest(Href_1.class, "HrefExpectedResult_1.html");
	}

	/**
	 * Simple Label
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2() throws Exception
	{
		tester.getApplication().getMarkupSettings().setStripWicketTags(true);
		executeTest(Href_1.class, "HrefExpectedResult_1-1.html");
	}

	/**
	 * Simple Label
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2a() throws Exception
	{
		tester.getApplication().getMarkupSettings().setStripWicketTags(true);
		executeTest(Href_2.class, "HrefExpectedResult_2.html");
	}

	/**
	 * Simple Label
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_3() throws Exception
	{
		executeTest(Href_3.class, "HrefExpectedResult_3.html");
	}
}
