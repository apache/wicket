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
package org.apache.wicket.markup.head;

import static org.junit.Assert.assertEquals;

import org.apache.wicket.markup.html.basic.SimplePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;


public class HtmlImportHeaderItemTest
{
	@Test
	public void basicUsage() throws Exception
	{
		String url = "/path/to/page.html";
		
		MetaDataHeaderItem result = HtmlImportHeaderItem.forImportLinkTag(url);
		
		assertEquals("<link rel=\"import\" href=\"" + url + "\" />\n", result.generateString());
		
		result = HtmlImportHeaderItem.forImportLinkTag(url, true);
		
		assertEquals("<link rel=\"import\" href=\"" + url + "\" async />\n", result.generateString());		
	}
	
	@Test
	public void wicketPageUrl() throws Exception
	{
		WicketTester tester = new WicketTester();	
		PageParameters parameters = new PageParameters();
		parameters.add("foo", "foo");
		parameters.add("bar", "bar");
		
		CharSequence pageUrl = tester.getRequestCycle().urlFor(SimplePage.class, parameters);
		
		MetaDataHeaderItem importLink = HtmlImportHeaderItem
			.forImportLinkTag(SimplePage.class, parameters, "monitor", true);
		
		assertEquals("<link rel=\"import\" href=\"" + pageUrl + "\" media=\"monitor\" async />\n", 
			importLink.generateString());
	}
}
