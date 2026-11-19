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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link XmlPartialPageUpdate}.
 */
class XmlPartialPageUpdateTest extends WicketTestCase
{

	/**
	 * CData start "]]>" has to be encoded in "]]]]><![CDATA[>".
	 */
	@Test
	void encodeCdataEnd()
	{
		PageForPartialUpdate page = new PageForPartialUpdate();
		
		XmlPartialPageUpdate update = new XmlPartialPageUpdate(page);
		
		update.add(page.container, page.container.getMarkupId());
		
		MockWebResponse response = new MockWebResponse();
		
		update.writeTo(response, "UTF-8");
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ajax-response><component id=\"container1\" ><![CDATA[<span wicket:id=\"container\" id=\"container1\"> two brackets: ]] greater than: > CDATA end: ]]]]><![CDATA[> </span>]]></component><header-contribution><![CDATA[<head xmlns:wicket=\"http://wicket.apache.org\"><script type=\"text/javascript\">\n" + 
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
	void keepTheSameHeaderContainer() throws Exception
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
	void removedComponentAreNotWritten() throws Exception
	{
		PageForPartialUpdate page = new PageForPartialUpdate();
		
		tester.startPage(page);
		
		XmlPartialPageUpdate update = new XmlPartialPageUpdate(page);		
		
		update.add(new Label("notInPage"), "notInPage");
		
		MockWebResponse response = new MockWebResponse();
		
		update.writeTo(response, "UTF-8");
		
		assertFalse(response.getTextResponse().toString().contains("notInPage"), "notInPage not written");
	}

	@Test
	void dataEntriesAreWrittenAfterEvaluationsInOrder()
	{
		PageForPartialUpdate page = new PageForPartialUpdate();

		XmlPartialPageUpdate update = new XmlPartialPageUpdate(page);
		update.addData("first", "{\"a\":1}");
		update.appendJavaScript("console.log('evaluated');");
		update.addData("second", "two");

		MockWebResponse response = new MockWebResponse();
		update.writeTo(response, "UTF-8");

		String text = response.getTextResponse().toString();
		assertTrue(text.endsWith("</evaluate><data name=\"first\"><![CDATA[{\"a\":1}]]></data>"
			+ "<data name=\"second\"><![CDATA[two]]></data></ajax-response>"), text);
	}

	@Test
	void dataNameIsEscapedAndCdataEndIsEncoded()
	{
		PageForPartialUpdate page = new PageForPartialUpdate();

		XmlPartialPageUpdate update = new XmlPartialPageUpdate(page);
		update.addData("a\"b<c", "x]]>y");

		MockWebResponse response = new MockWebResponse();
		update.writeTo(response, "UTF-8");

		assertTrue(response.getTextResponse()
			.toString()
			.contains("<data name=\"a&quot;b&lt;c\"><![CDATA[x]]]]><![CDATA[>y]]></data>"));
	}

	@Test
	void dataMakesTheUpdateNonEmpty()
	{
		XmlPartialPageUpdate update = new XmlPartialPageUpdate(new PageForPartialUpdate());
		assertTrue(update.isEmpty());

		update.addData("name", "");

		assertFalse(update.isEmpty());
	}

	@Test
	void dataCannotBeAddedOnceTheResponseIsBeingWritten()
	{
		PageForPartialUpdate page = new PageForPartialUpdate();

		XmlPartialPageUpdate update = new XmlPartialPageUpdate(page)
		{
			@Override
			protected void onAfterRespond(Response response)
			{
				addData("allowed", "until the components are written");
			}

			@Override
			protected void writeFooter(Response response, String encoding)
			{
				assertThrows(IllegalStateException.class, () -> addData("late", "too late"));
				super.writeFooter(response, encoding);
			}
		};

		MockWebResponse response = new MockWebResponse();
		update.writeTo(response, "UTF-8");

		assertTrue(response.getTextResponse().toString().contains("<data name=\"allowed\">"));
	}
}
