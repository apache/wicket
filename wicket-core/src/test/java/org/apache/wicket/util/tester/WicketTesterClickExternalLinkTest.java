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
package org.apache.wicket.util.tester;

import org.apache.wicket.MockPageWithLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.junit.Test;

/**
 * Test for https://issues.apache.org/jira/browse/WICKET-5840
 */
public class WicketTesterClickExternalLinkTest extends WicketTestCase
{
	@Test
	public void clickExternalLink()
	{
		MockPageWithLink page = new MockPageWithLink();
		String href = "http://wicket.apache.org";
		page.add(new ExternalLink(MockPageWithLink.LINK_ID, href, "Wicket site"));

		tester.startPage(page);
		tester.clickLink(MockPageWithLink.LINK_ID, false);

		tester.assertRedirectUrl(href);
	}
}
