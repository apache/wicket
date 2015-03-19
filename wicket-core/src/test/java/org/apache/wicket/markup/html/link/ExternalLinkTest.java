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

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

/**
 * Test ExternalLink (href="...")
 * 
 * <a href="https://issues.apache.org/jira/browse/WICKET-1016"></<a>
 */
public class ExternalLinkTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	public void renderExternalLink_1() throws Exception
	{
		tester.getApplication().getMarkupSettings().setAutomaticLinking(true);
		executeTest(ExternalLinkPage_1.class, "ExternalLinkPageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderExternalLink_2() throws Exception
	{
		tester.getApplication().getMarkupSettings().setAutomaticLinking(true);
		executeTest(ExternalLinkPage_2.class, "ExternalLinkPageExpectedResult_2.html");
	}

}
