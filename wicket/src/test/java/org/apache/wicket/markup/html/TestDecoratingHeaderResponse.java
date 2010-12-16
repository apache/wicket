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
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;

/**
 * @author Pedro Santos
 */
public class TestDecoratingHeaderResponse extends WicketTestCase
{
	private static final ResourceReference JS_REF = new ResourceReference("test.js");
	private static final String TEST_JS_REF_ID = "test-resource";


	/**
	 * Basic IHeaderResponseDecorator, just prepending the DECORATED string to resource name.
	 * 
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 * @throws ParseException
	 */
	public void testDecoratedStringPrepend() throws IOException, ResourceStreamNotFoundException,
		ParseException
	{
		tester.getApplication().setHeaderResponseDecorator(new IHeaderResponseDecorator()
		{
			public IHeaderResponse decorate(IHeaderResponse response)
			{
				return new DecoratingHeaderResponse(response)
				{
					@Override
					public void renderJavascriptReference(ResourceReference reference, String id)
					{
						super.renderJavascriptReference(new ResourceReference("DECORATED-" +
							reference.getName()), id);
					}
				};
			}
		});
		tester.startPage(TestPage.class);
		XmlPullParser parser = new XmlPullParser();
		parser.parse(tester.getServletResponse().getDocument());
		XmlTag tag = (XmlTag)parser.nextTag();
		boolean isDecorated = false;
		do
		{
			if ("script".equals(tag.getName()) && TEST_JS_REF_ID.equals(tag.getString("id")))
			{
				isDecorated = tag.getString("src").toString().contains("DECORATED");
			}
		}
		while ((tag = (XmlTag)parser.nextTag()) != null);
		assertTrue(isDecorated);
	}

	public static class TestPage extends WebPage
		implements
			IHeaderContributor,
			IMarkupResourceStreamProvider
	{

		public void renderHead(IHeaderResponse response)
		{
			response.renderJavascriptReference(JS_REF, TEST_JS_REF_ID);
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body></body></html>");
		}
	}

}
