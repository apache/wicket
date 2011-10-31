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
package org.apache.wicket.markup.html.autolink;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.autolink.sub.PageB;
import org.apache.wicket.markup.resolver.AutoLinkResolver;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 */
public class AutolinkTest extends WicketTestCase
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(AutoLinkResolver.class);

	/**
	 * 
	 * @throws Exception
	 */
	public void test_1() throws Exception
	{
		executeTest(MyPage.class, "MyPageExpectedResult.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_2() throws Exception
	{
		executeTest(PageA.class, "PageAExpectedResult.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_3() throws Exception
	{
		executeTest(PageB.class, "PageBExpectedResult.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_4() throws Exception
	{
		executeTest(Index.class, "Index_ExpectedResult.html");
	}
}
