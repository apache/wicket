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

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Timo Rantalaiho
 */
public class MarkupCacheTest extends WicketTestCase
{
	private MarkupCache cache;
	private MarkupCachingAssumingComponent component;

	/**
	 * 
	 */
	@Before
	public void before()
	{
		cache = new MarkupCache();

		Application.get().getMarkupSettings().setMarkupFactory(new MarkupFactory()
		{
			@Override
			public IMarkupCache getMarkupCache()
			{
				return cache;
			}
		});

		component = new MarkupCachingAssumingComponent("panel");
		tester.startComponentInPage(component);
	}

	/**
	 * testMarkupNotFoundInformationIsCachedInDeploymentMode()
	 */
	@Test
	public void markupNotFoundInformationIsCachedInDeploymentMode()
	{
		IMarkupFragment markup = cache.getMarkup(component, null, false);
		assertNotNull(markup);

		markup = cache.getMarkup(component, null, false);
		assertNotNull(markup);
	}

	/**
	 * testRemoveMarkupWhereBaseMarkupIsNoLongerInTheCache()
	 */
	@Test
	public void removeMarkupWhereBaseMarkupIsNoLongerInTheCache()
	{
		tester.startPage(MarkupInheritanceExtension_1.class);
		tester.assertRenderedPage(MarkupInheritanceExtension_1.class);
		MarkupInheritanceExtension_1 page = (MarkupInheritanceExtension_1)tester.getLastRenderedPage();

		IMarkupFragment markup = cache.getMarkup(page, null, false);
		assertNotNull(markup);

		String key = markup.getMarkupResourceStream().getBaseMarkupResourceStream().getCacheKey();
		cache.removeMarkup(key);

		markup = cache.getMarkupFromCache(markup.getMarkupResourceStream().getCacheKey(), page);
		assertNull(markup);
	}

	private static class MarkupCachingAssumingComponent extends Panel
		implements
			IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = -6743937191677599322L;
		private boolean firstCall = true;

		public MarkupCachingAssumingComponent(final String id)
		{
			super(id);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			if (firstCall)
			{
				firstCall = false;
				return new StringResourceStream("<wicket:panel><div></div></wicket:panel>");
			}
			fail("Markup should be cached");
			throw new IllegalStateException();
		}

		@Override
		public MarkupType getMarkupType()
		{
			return MarkupType.HTML_MARKUP_TYPE;
		}
	}
}
