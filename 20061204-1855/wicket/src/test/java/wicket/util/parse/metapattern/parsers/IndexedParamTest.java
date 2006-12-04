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
package wicket.util.parse.metapattern.parsers;

import wicket.WicketTestCase;
import wicket.protocol.http.MockHttpServletRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.request.target.coding.IndexedParamUrlCodingStrategy;
import wicket.util.diff.DiffUtil;

/**
 * Test [ 1470093 ] <wicket:link> does not accept numeric param names
 * 
 * @author Juergen Donnerstag
 * @author Blake Day 
 */
public class IndexedParamTest extends WicketTestCase
{
	/**
	 * Construct.
	 * @param name
	 */
	public IndexedParamTest(String name)
	{
		super(name);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testPage() throws Exception
	{
		application.setHomePage(Page1.class);
		application.mount("/page2", new IndexedParamUrlCodingStrategy("/page2", Page2.class));

		executeTest(Page1.class, "IndexedParamTest_ExpectedResult-1.html");

		// Click the autolink
		application.setupRequestAndResponse();
		WebRequestCycle cycle = application.createRequestCycle();
		((MockHttpServletRequest)application.getWicketRequest().getHttpServletRequest()).setURL("/WicketTester/WicketTester/page2/abc");
		application.processRequestCycle(cycle);
		
		assertEquals(Page2.class, application.getLastRenderedPage().getClass());

		// Validate the document
		String document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(), "IndexedParamTest_ExpectedResult-2.html"));
	}
}
