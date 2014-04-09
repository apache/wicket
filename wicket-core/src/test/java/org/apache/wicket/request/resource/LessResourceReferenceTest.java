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
package org.apache.wicket.request.resource;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Before;
import org.junit.Test;

/**
 * <a href"https://issues.apache.org/jira/browse/WICKET-4732">WICKET-4732</a>
 *
 * @since 1.5.9
 */
public class LessResourceReferenceTest extends WicketTestCase
{
	private static final AtomicBoolean PROCESS_RESPONSE_CALLED = new AtomicBoolean(false);

	/**
	 * An {@link IResourceReferenceFactory} that creates
	 * LessResourceReference for resources with extension '.less'
	 */
	private static class LessResourceReferenceFactory extends ResourceReferenceRegistry.DefaultResourceReferenceFactory
	{
		@Override
		public ResourceReference create(ResourceReference.Key key)
		{
			ResourceReference result = null;
			if (PackageResource.exists(key))
			{
				if ("less".equals(Files.extension(key.getName())))
				{
					result = new LessResourceReference(key);
				}
				else
				{
					result = super.create(key);
				}
			}
			return result;
		}
	}

	@Before
	public void before()
	{
		PROCESS_RESPONSE_CALLED.set(false);
	}

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();

				SecurePackageResourceGuard guard = (SecurePackageResourceGuard) getResourceSettings().getPackageResourceGuard();
				guard.addPattern("+*.less");
			}

			/**
			 * Register custom ResourceReferenceRegistry that creates LessResourceReference
			 * for requests with .less extension
			 */
			@Override
			protected ResourceReferenceRegistry newResourceReferenceRegistry()
			{
				return new ResourceReferenceRegistry(new LessResourceReferenceFactory());
			}
		};
	}

	/**
	 * Tests that LessResourceReference is properly registered in ResourceReferenceRegistry
	 * and later when making a request for it will actually resolve the registered reference
	 * instead of automatically created PackageResourceReference
	 */
	@Test
	public void processLessResources()
	{
		// load the page to register the resource
		LessResourcePage page = new LessResourcePage();
		tester.startPage(page);
		assertFalse(PROCESS_RESPONSE_CALLED.get());

		// make a request to the resource and assert
		CharSequence urlToReference = page.urlFor(page.resourceReference, null);
		tester.executeUrl(urlToReference.toString());
		assertTrue(PROCESS_RESPONSE_CALLED.get());
	}

	/**
	 * Tests that a LessResourceReference is request-able without being registered in ResourceReferenceRegistry
	 */
	@Test
	public void processLessResourcesWithoutStartingAPage()
	{
		// make a request to the resource that is not registered in the ResourceReferenceRegistry
		CharSequence urlToReference = "./wicket/resource/org.apache.wicket.request.resource.LessResourceReferenceTest/LessResourceReference.less";
		tester.executeUrl(urlToReference.toString());
		assertTrue(PROCESS_RESPONSE_CALLED.get());
	}

	private static class LessResourcePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private final LessResourceReference resourceReference = new LessResourceReference(LessResourceReferenceTest.class, "LessResourceReference.less");

		@Override
		public void renderHead(IHeaderResponse response)
		{
			super.renderHead(response);
			response.render(CssHeaderItem.forReference(resourceReference));
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html/>");
		}
	}

	private static class LessResourceReference extends CssResourceReference
	{
		public LessResourceReference(Class<?> scope, String name)
		{
			super(scope, name);
		}

		public LessResourceReference(Key key)
		{
			super(key);
		}

		@Override
		public LessPackageResource getResource()
		{
			return new LessPackageResource(getScope(), getName());
		}
	}

	private static class LessPackageResource extends CssPackageResource
	{
		public LessPackageResource(Class<?> scope, String name)
		{
			super(scope, name, null, null, null);
		}

		@Override
		protected byte[] processResponse(Attributes attributes, byte[] bytes)
		{
			PROCESS_RESPONSE_CALLED.set(true);
			return super.processResponse(attributes, bytes);
		}
	}
}
