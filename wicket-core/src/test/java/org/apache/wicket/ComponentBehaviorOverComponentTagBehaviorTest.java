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
package org.apache.wicket;

import java.util.Locale;

import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * Tests that behaviors added to a Component has precedence
 * over similar behavior added on the ComponentTag for this Component
 *
 * https://issues.apache.org/jira/browse/WICKET-4369
 *
 * @since 1.5.5
 */
public class ComponentBehaviorOverComponentTagBehaviorTest extends WicketTestCase
{
	/**
	 * Verifies that the Label's title attribute comes from the
	 * Component behavior instead of ComponentTag's behavior (added by
	 * WicketMessageTagHandler)
	 */
	@Test
	public void runtimeBehaviorOverMarkupBehavior()
	{
		tester.getApplication().getResourceSettings().getStringResourceLoaders().add(new TestStringResourceLoader());
		WicketMessage page = new WicketMessage();
		tester.startPage(page);

		tester.assertContains("title=\"Component behavior title\"");
	}

	private static class TestStringResourceLoader implements IStringResourceLoader
	{
		@Override
		public String loadStringResource(Class<?> clazz, String key, Locale locale, String style, String variation)
		{
			return "markupTitle".equals(key) ? "ComponentTag behavior title" : null;
		}

		@Override
		public String loadStringResource(Component component, String key, Locale locale, String style, String variation)
		{
			return loadStringResource(component.getClass(), key, locale, style, variation);
		}
	}
	
	private static class WicketMessage extends WebPage implements IMarkupResourceStreamProvider
	{
		private WicketMessage() {

			Label label = new Label("l", "Label");
			label.add(AttributeModifier.replace("title", "Component behavior title"));
			add(label);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" +
					"<span wicket:id=\"l\" wicket:message=\"title:markupTitle\">Test</span>" +
					"</body></html>");
		}
	}
}
