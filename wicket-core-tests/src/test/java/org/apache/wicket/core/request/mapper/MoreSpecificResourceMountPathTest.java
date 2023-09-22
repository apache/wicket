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
package org.apache.wicket.core.request.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-6094
 */
class MoreSpecificResourceMountPathTest
{
	@Test
	void can_use_resource_mounted_without_parameter()
	{
		WicketTester browser = new WicketTester(new WicketApplication());
		browser.executeUrl(WicketApplication.urlFor("howdy"));
		assertEquals("howdy", browser.getLastResponseAsString());
	}

	@Test
	void can_use_resource_mounted_with_parameter()
	{
		WicketTester browser = new WicketTester(new WicketApplication());
		browser.executeUrl(WicketApplication.urlFor(1L));
		assertEquals("1", browser.getLastResponseAsString());
	}

	static class WicketApplication extends WebApplication
	{
		static final String PARAM_ID = "id";
		static final String PARAM_NAME = "name";
		private static final String OWNER_BY_ID_LOADER = "owner-by-id-loader";
		private static final String OWNERS_LISTER = "owners-lister";

		static String urlFor(String name)
		{
			return urlFor(name, PARAM_NAME, OWNERS_LISTER);
		}

		static String urlFor(Long id)
		{
			return urlFor(id, PARAM_ID, OWNER_BY_ID_LOADER);
		}

		/**
		 * <Test-Helper> Generate an {@link URL} to access the mounted resource reference.
		 *
		 * @param value
		 *            of dummy attribute used to have some testable response output.
		 * @param parameterName
		 *            of dummy attribute
		 * @param resourceReferenceName
		 *            used to mount instance
		 * @return {@link CharSequence} url for resource reference
		 */
		private static String urlFor(Object value, String parameterName,
			String resourceReferenceName)
		{
			PageParameters parameters = new PageParameters();
			if (value != null)
			{
				parameters.set(parameterName, value);
			}
			ResourceReference resourceReference = findResourceReference(resourceReferenceName);
			String string = RequestCycle.get().urlFor(resourceReference, parameters).toString();
			return string;
		}

		/**
		 * <Test-Helper> Find resource reference mounted in application.
		 *
		 * @param name
		 *            of resource reference used to mount instance
		 * @return {@link ResourceReference} found
		 */
		private static ResourceReference findResourceReference(String name)
		{
			return Application.get().getResourceReferenceRegistry().getResourceReference(
				new ResourceReference.Key(Application.class.getName(), name, null, null, null),
				false, false);
		}

		@Override
		public Class<? extends WebPage> getHomePage()
		{
			return DummyHomePage.class;
		}

		@Override
		public void init()
		{
			super.init();

			String path = "/ajax/owners"; // shared by both references

			mountResource(path, new ResourceReference(OWNERS_LISTER)
			{
				@Override
				public IResource getResource()
				{
					return new DummyResource(PARAM_NAME);
				}
			});

			mountResource(path + "/${" + PARAM_ID + "}", new ResourceReference(OWNER_BY_ID_LOADER)
			{
				@Override
				public IResource getResource()
				{
					return new DummyResource(PARAM_ID);
				}
			});
		}

		/**
		 * <Test-Helper> This is only a dummy to be referenced. It is possible to exchange this by a
		 * mock or whatever.
		 * 
		 * @author rene.dieckmann@menoto.de
		 */
		private static class DummyResource extends ByteArrayResource
		{
			private final String parameterName;

			DummyResource(String parameterName)
			{
				super("application/text");
				this.parameterName = parameterName;
			}

			@Override
			protected byte[] getData(Attributes attributes)
			{
				StringValue value = attributes.getParameters().get(parameterName);
				return value == null ? new byte[0] : value.toString().getBytes();
			}
		}
	}
}
