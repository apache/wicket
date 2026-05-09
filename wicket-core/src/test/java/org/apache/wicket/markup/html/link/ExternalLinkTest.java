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

import org.apache.wicket.MockPageWithOneComponent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

import static org.apache.wicket.MockPageWithOneComponent.COMPONENT_ID;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * Test ExternalLink (href="...")
 * 
 * <a href="https://issues.apache.org/jira/browse/WICKET-1016"></<a>
 */
public class ExternalLinkTest extends WicketTestCase
{

	@Test
	public void allowsJavascriptScheme() throws Exception
	{
		String uri = "javascript:alert(1)";
		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(new ExternalLink(COMPONENT_ID, uri){
			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.setName("a");
			}
		});

		tester.startPage(page);

		assertThat(tester.getLastResponseAsString(), containsString(uri));
	}

	@Test
	public void allowsJavascriptSchemeInPopupTarget() throws Exception
	{
		String uri = "javascript:alert(1)";
		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(new ExternalLink(COMPONENT_ID, uri));

		tester.startPage(page);

		assertThat(tester.getLastResponseAsString(), containsString(uri));
	}

	@Test
	public void escapesJavascriptQuotes() throws Exception
	{
		String unescaped = "javascript:alert('foo')";
		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(new ExternalLink(COMPONENT_ID, unescaped));

		tester.startPage(page);

		assertThat(tester.getLastResponseAsString(), containsString("javascript:alert(&#039;foo&#039;)"));
	}

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
