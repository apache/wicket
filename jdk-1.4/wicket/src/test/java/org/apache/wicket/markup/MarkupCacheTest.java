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

import junit.framework.TestCase;
import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.tester.WicketTester;

/**
 * @author Timo Rantalaiho
 */
public class MarkupCacheTest extends TestCase
{
	private MarkupCache cache;
	private MarkupCachingAssumingComponent component;

	public void setUp() {
		WebApplication application = new WicketTester.DummyWebApplication() {
			public String getConfigurationType()
			{
				return Application.DEPLOYMENT;
			}
		};
		WicketTester tester = new WicketTester(application);
		cache = new MarkupCache(application);

		component = new MarkupCachingAssumingComponent("panel");
		tester.startComponent(component);
	}

	public void testMarkupNotFoundInformationIsCachedInDeploymentMode() {
		Markup markup = cache.getMarkup(component, null, false);
		assertEquals(Markup.NO_MARKUP, markup);

		markup = cache.getMarkup(component, null, false);
		assertEquals(Markup.NO_MARKUP, markup);
	}

	private class MarkupCachingAssumingComponent extends Panel implements IMarkupResourceStreamProvider {
		private static final long serialVersionUID = -6743937191677599322L;
		private boolean firstCall = true;

		public MarkupCachingAssumingComponent(final String id)
		{
			super(id);
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class containerClass)
		{
			if (firstCall) {
				firstCall = false;
				return null;
			}
			fail("Markup should be cached");
			throw new IllegalStateException();
		}

		public String getMarkupType()
		{
			return "html";
		}
	}
}
