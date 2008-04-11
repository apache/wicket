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
package org.apache.wicket;

import junit.framework.TestCase;

import org.apache.wicket.ajax.markup.html.componentMap.SimpleTestPage;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.panel.InlinePanelPage_1;
import org.apache.wicket.util.tester.WicketTester;

/**
 * @author Juergen Donnerstag
 */
public class MarkupFragmentFinderTest extends TestCase
{
	/**
	 * Test method for
	 * {@link org.apache.wicket.MarkupFragmentFinder#find(org.apache.wicket.Component)}.
	 */
	public void testFind()
	{
		WicketTester tester = new WicketTester();
		Page page = new SimpleTestPage();
		Component panel = page.get("testPanel");
		assertNotNull(panel);
		MarkupStream markupStream = new MarkupFragmentFinder().find(panel);
		assertNotNull(markupStream);

		panel = page.get("testPanel:baseSpan");
		assertNotNull(panel);
		markupStream = new MarkupFragmentFinder().find(panel);
		assertNotNull(markupStream);

		page = new InlinePanelPage_1();
		panel = page.get("myPanel1");
		assertNotNull(panel);
		markupStream = new MarkupFragmentFinder().find(panel);
		assertNotNull(markupStream);
	}
}
