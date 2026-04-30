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

import org.apache.wicket.MockPageWithLink;
import org.apache.wicket.MockPageWithOneComponent;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

import static org.apache.wicket.MockPageWithOneComponent.COMPONENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinkTest extends WicketTestCase
{

	@Test
	void allowsJavascriptSchemeInPopupsTarget()
	{
		var uri = "javascript:alert(1);";
		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(new PopupLink(COMPONENT_ID, uri));

		tester.startPage(page);

		assertThat(tester.getLastResponseAsString()).contains(uri);
	}

	@Test
	void escapesJavascriptQuotesInPopupsTarget()
	{
		var uri = "javascript:alert('foo');";
		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(new PopupLink(COMPONENT_ID, uri));

		tester.startPage(page);

		assertThat(tester.getLastResponseAsString()).contains("javascript:alert(\\'foo\\');");
	}

	@Test
	void testWrongComponentId()
	{
		MockPageWithLink mockPageWithLink = new MockPageWithLink();
		Link<Void> link = new Link<Void>("linkx")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
			}

		};

		mockPageWithLink.add(link);
		assertThrows(WicketRuntimeException.class, () -> tester.startPage(mockPageWithLink));
	}

	static class PopupLink extends Link<Void>
	{
		private final String uri;

		public PopupLink(String id, String uri)
		{
			super(id);
			this.uri = uri;
			setPopupSettings(new PopupSettings());
		}

		@Override
		public void onClick()
		{
		}

		@Override
		protected void onComponentTag(ComponentTag tag)
		{
			super.onComponentTag(tag);
			tag.setName("a");
		}

		@Override
		protected CharSequence getURL()
		{
			return uri;
		}
	}

}