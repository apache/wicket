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
package org.apache.wicket.markup;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * @author Pedro Santos
 */
class ComponentTagAttributeEscapingTest extends WicketTestCase
{

	/**
	 * @throws Exception
	 */
	@Test
	void componentAttributesNotDoubleEscaped() throws Exception
	{
		tester.startPage(ButtonValuePage.class);
		String response = tester.getLastResponseAsString();
// System.out.println(response);
		assertTrue(response.contains("\u00a3\u00a3"),
			"One of the pound entity representations is missing: &pound; or &#163;");
		assertTrue(response.contains("Watch escaped value: &gt;&gt;"),
			"must not be double escaped");
		assertTrue(response.contains("alerting: &amp;"),
			"following the last assert logic, this one would true");
		assertTrue(response.contains("some_attribute=\"a &amp; b\""),
			"escape manually added attributes");
	}

	/**
	 * Just two distinct components with escaped characters in markup attribute.
	 */
	public static class ButtonValuePage extends WebPage implements IMarkupResourceStreamProvider
	{
		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
        public ButtonValuePage()
		{
			add(new Button("button"));
			add(new Link<Void>("link")
			{
				/** */
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
				}

				@Override
				protected void onComponentTag(ComponentTag tag)
				{
					super.onComponentTag(tag);
					tag.put("some_attribute", "a & b");
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html>"//
				+ "<body>"//
				+
				"<a wicket:id=\"link\" onclick=\"alert('alerting: &amp; &pound;&#163; ')\">link</a>"//
				+
				"<input type=\"submit\" wicket:id=\"button\" value=\"Watch escaped value: &gt;&gt;\"/>"//
				+ "</body>" + //
				"</html>");
		}
	}
}