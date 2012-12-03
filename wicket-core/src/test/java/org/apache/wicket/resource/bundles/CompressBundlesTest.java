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
package org.apache.wicket.resource.bundles;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.javascript.DefaultJavaScriptCompressor;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * A test case for https://issues.apache.org/jira/browse/WICKET-4902
 */
public class CompressBundlesTest extends WicketTestCase
{
	/**
	 * Asserts that the bundle response is compressed
	 */
	@Test
	public void compressBundle()
	{
		tester.startPage(new CompressBundleTestPage());
		tester.assertRenderedPage(CompressBundleTestPage.class);

		tester.executeUrl("wicket/resource/org.apache.wicket.resource.bundles.CompressBundlesTest/bundle.js");

		String expected = "\nvar two = function() {\nconsole.log( 2 );\n};\nvar one = function() {\nconsole.log( 1 );\n};";
		assertEquals(expected, tester.getLastResponse().getDocument());
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

				// remove that setting to see the original (non-compressed) version of the bundle
				getResourceSettings().setJavaScriptCompressor(new DefaultJavaScriptCompressor());

				getResourceBundles().addJavaScriptBundle(CompressBundlesTest.class, "bundle.js",
					new JavaScriptResourceReference(CompressBundlesTest.class, "two.js"),
					new JavaScriptResourceReference(CompressBundlesTest.class, "one.js")
				);
			}
		};
	}

	private static class CompressBundleTestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		public CompressBundleTestPage() {}

		@Override
		public void renderHead(IHeaderResponse response)
		{
			super.renderHead(response);

			response.render(JavaScriptHeaderItem.forReference(
					new JavaScriptResourceReference(CompressBundlesTest.class, "one.js")));

			response.render(JavaScriptHeaderItem.forReference(
					new JavaScriptResourceReference(CompressBundlesTest.class, "three.js")));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body></body></html>");
		}
	}
}
