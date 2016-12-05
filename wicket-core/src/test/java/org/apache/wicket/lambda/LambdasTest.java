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
package org.apache.wicket.lambda;

import static org.apache.wicket.lambda.Lambdas.onTag;
import static org.apache.wicket.lambda.Lambdas.onAttribute;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.apache.wicket.MockPageWithOneComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for {@link Lambdas}
 */
public class LambdasTest extends WicketTestCase
{
	@Test
	public void onTagTest()
	{
		WebMarkupContainer component = new WebMarkupContainer(MockPageWithOneComponent.COMPONENT_ID);
		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(component);
		String value = "value";
		String key = "key";
		component.add(onTag((c, tag) -> tag.put(key, value)));
		component.add(onAttribute("class", oldValue -> "zzz"));

		tester.startPage(page);

		TagTester tagTester = tester.getTagByWicketId(MockPageWithOneComponent.COMPONENT_ID);
		assertThat(tagTester.getAttribute(key), is(equalTo(value)));
		assertThat(tagTester.getAttribute("class"), is(equalTo("zzz")));
	}
	
	@Test
	public void onAttributeTest()
	{
		WebMarkupContainer component = new WebMarkupContainer(MockPageWithOneComponent.COMPONENT_ID);
		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(component);
		String value = "value";
		String key = "key";
		component.add(onAttribute(key, oldValue -> value));

		tester.startPage(page);

		TagTester tagTester = tester.getTagByWicketId(MockPageWithOneComponent.COMPONENT_ID);
		assertThat(tagTester.getAttribute(key), is(equalTo(value)));
	}
}
