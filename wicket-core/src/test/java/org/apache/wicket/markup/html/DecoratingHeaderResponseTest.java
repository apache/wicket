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
package org.apache.wicket.markup.html;

import java.io.IOException;
import java.text.ParseException;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class DecoratingHeaderResponseTest extends WicketTestCase
{

	/**
	 * Basic IHeaderResponseDecorator, just prepending the DECORATED string to resource name.
	 * 
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 * @throws ParseException
	 */
	@Test
	public void decoratedStringPrepend() throws IOException, ResourceStreamNotFoundException,
		ParseException
	{
		tester.getApplication().setHeaderResponseDecorator(new IHeaderResponseDecorator()
		{
			@Override
			public IHeaderResponse decorate(IHeaderResponse response)
			{
				return new DecoratingHeaderResponse(response)
				{
					@Override
					public void render(HeaderItem item)
					{
						if (item instanceof JavaScriptReferenceHeaderItem)
						{
							JavaScriptReferenceHeaderItem original = (JavaScriptReferenceHeaderItem)item;
							item = JavaScriptHeaderItem.forReference(new PackageResourceReference(
								"DECORATED-" + original.getReference().getName()), original.getId());
						}
						super.render(item);
					}
				};
			}
		});
		tester.startPage(TestPage.class);
		XmlPullParser parser = new XmlPullParser();
		parser.parse(tester.getLastResponseAsString());
		XmlTag tag = parser.nextTag();
		boolean isDecorated = false;
		do
		{
			if (tag.isOpen() && "script".equals(tag.getName()))
			{
				isDecorated = tag.getAttribute("src").toString().contains("DECORATED");
				if (!isDecorated)
				{
					fail();
				}
				break;
			}
		}
		while ((tag = parser.nextTag()) != null);
		assertTrue(isDecorated);
	}

	/**
	 * 
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void renderHead(IHeaderResponse response)
		{
			for (int i = 0; i < 10; i++)
			{
				response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(
						"res" + i), Integer.toString(i)));
			}
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body></body></html>");
		}
	}
}
