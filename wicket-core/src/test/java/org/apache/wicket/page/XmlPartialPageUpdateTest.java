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
package org.apache.wicket.page;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for {@link XmlPartialPageUpdate}.
 */
public class XmlPartialPageUpdateTest extends WicketTestCase 
{

	/**
	 * CData start "]]>" has to be encoded in "]]]]><![CDATA[>".
	 */
	@Test
	public void encodeCdataEnd() 
	{
		PageForPartialUpdate page = new PageForPartialUpdate();
		
		XmlPartialPageUpdate update = new XmlPartialPageUpdate(page);
		
		update.add(page.container, page.container.getMarkupId());
		
		MockWebResponse response = new MockWebResponse();
		
		update.writeTo(response, "UTF-8");
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ajax-response><component id=\"container1\" ><![CDATA[<span wicket:id=\"container\" id=\"container1\"> two brackets: ]] greater than: > CDATA end: ]]]]><![CDATA[> </span>]]></component><header-contribution><![CDATA[<head xmlns:wicket=\"http://wicket.apache.org\"><script type=\"text/javascript\" >\n" + 
				"/*<![CDATA[*/\n" + 
				"// two brackets: ]] greater than: > CDATA end: ]]]]><![CDATA[>\n" + 
				"/*]]]]><![CDATA[>*/\n" + 
				"</script>\n" + 
				"</head>]]></header-contribution></ajax-response>";
		assertEquals(expected, response.getTextResponse().toString());
	}
	
	/**
	 * 
	 * see https://issues.apache.org/jira/browse/WICKET-6162
	 */
	@Test
	public void keepTheSameHeaderContainer() throws Exception
	{
		PageForPartialUpdate page = new PageForPartialUpdate();
		
		tester.startPage(page);
		
		Component originalHeader = page.get(HtmlHeaderSectionHandler.HEADER_ID);
		
		XmlPartialPageUpdate update = new XmlPartialPageUpdate(page);		
		
		update.add(page.container, page.container.getMarkupId());
		
		MockWebResponse response = new MockWebResponse();
		
		update.writeTo(response, "UTF-8");
		
		assertEquals(originalHeader, page.get(HtmlHeaderSectionHandler.HEADER_ID));
	}
	
	/**
	 * WICKET-6503 removed components are not written, but no exception raised either. 
	 */
	@Test
	public void removedComponentAreNotWritten() throws Exception
	{
		PageForPartialUpdate page = new PageForPartialUpdate();
		
		tester.startPage(page);
		
		XmlPartialPageUpdate update = new XmlPartialPageUpdate(page);		
		
		update.add(new Label("notInPage"), "notInPage");
		
		MockWebResponse response = new MockWebResponse();
		
		update.writeTo(response, "UTF-8");
		
		assertFalse("notInPage not written", response.getTextResponse().toString().contains("notInPage"));
	}
}
