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
package org.apache.wicket.stateless;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * @author jcompagner
 */
public class ImageStatelessTest extends WicketTestCase
{
	/**
	 * 
	 */
	@Test
	public void resourceReference()
	{
		ImageStatelessPage page = new ImageStatelessPage();
		final Image i = new Image("test", new PackageResourceReference("test"));
		page.add(i);
		tester.startPage(page);
		assertTrue("image with resource reference should be stateless", i.isStateless());
	}

	/**
	 * 
	 */
	@Test
	public void resource()
	{
		ImageStatelessPage page = new ImageStatelessPage();
		final Image i = new Image("test", new ByteArrayResource("text/text", new byte[0]));
		page.add(i);
		tester.startPage(page);
		assertTrue("image with resource should be stateful", !i.isStateless());
	}

	private static class ImageStatelessPage extends WebPage implements IMarkupResourceStreamProvider
	{

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><img wicket:id='test'/></body></html>");
		}
	}
}
