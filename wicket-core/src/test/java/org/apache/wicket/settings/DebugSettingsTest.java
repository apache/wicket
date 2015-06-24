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
package org.apache.wicket.settings;

import org.apache.wicket.Component;
import org.apache.wicket.MockPageWithLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for DebugSettings
 */
public class DebugSettingsTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5498
	 */
	@Test
	public void setComponentPathAttributeName()
	{
		String attributeName = "data-wicket-path";
		tester.getApplication().getDebugSettings().setComponentPathAttributeName(attributeName);
		MockPageWithLink page = new MockPageWithLink();
		Component link = new Link(MockPageWithLink.LINK_ID)
		{
			@Override
			public void onClick()
			{
			}
		}.setMarkupId(MockPageWithLink.LINK_ID);
		page.add(link);
		tester.startPage(page);

		TagTester tagTester = tester.getTagById(MockPageWithLink.LINK_ID);
		String wicketPath = tagTester.getAttribute(attributeName);
		assertEquals(link.getPageRelativePath(), wicketPath);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5498
	 */
	@Test
	public void setComponentPathAttributeNameDeprected()
	{
		tester.getApplication().getDebugSettings().setOutputComponentPath(true);
		MockPageWithLink page = new MockPageWithLink();
		Component link = new Link(MockPageWithLink.LINK_ID)
		{
			@Override
			public void onClick()
			{
			}
		}.setMarkupId(MockPageWithLink.LINK_ID);
		page.add(link);
		tester.startPage(page);

		TagTester tagTester = tester.getTagById(MockPageWithLink.LINK_ID);
		String wicketPath = tagTester.getAttribute("wicketpath");
		assertEquals(link.getPageRelativePath(), wicketPath);
	}
}
