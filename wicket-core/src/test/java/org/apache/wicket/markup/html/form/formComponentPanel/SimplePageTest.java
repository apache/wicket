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
package org.apache.wicket.markup.html.form.formComponentPanel;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 */
public class SimplePageTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(SimplePageTest.class);

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage() throws Exception
	{
		tester.startComponentInPage(MyFormComponentPanel.class);
		String doc = tester.getLastResponseAsString();
		// log.error(doc);
		assertEquals("<span wicket:id=\"testObject\"><wicket:panel>test</wicket:panel></span>", doc);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3700
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderHomePageWithHeaderContribution() throws Exception
	{
		tester.startComponentInPage(MyFormComponentPanel.class);
		String doc = tester.getLastResponse().getDocument();
		// log.error(doc);
		assertTrue(doc.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"../some.css\" />"));
	}
}
