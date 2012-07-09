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
package org.apache.wicket.ajax.markup.html;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * @since 6.0.0
 */
public class AjaxFallbackLinkTest extends WicketTestCase
{
	/**
	 * Tests that AjaxFallbackLink doesn't produce onclick inline attribute for non-anchor markup elements
	 *
	 * https://issues.apache.org/jira/browse/WICKET-4644
	 */
	@Test
	public void noInlineOnClickAttribute()
	{
		tester.startPage(new AjaxFallbackLinkPage());
		tester.assertContainsNot("onclick=");
	}

	private static class AjaxFallbackLinkPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private AjaxFallbackLinkPage()
		{
			add(new AjaxFallbackLink("l") {

				@Override
				public void onClick(AjaxRequestTarget target)
				{
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><bla wicket:id='l'>Ajax fallback link</bla></body></html>");
		}
	}
}
