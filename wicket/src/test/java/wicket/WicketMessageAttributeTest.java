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
package wicket;

import java.util.Locale;

import wicket.markup.IMarkupResourceStreamProvider;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.resource.IPropertiesFactory;
import wicket.resource.IPropertiesReloadListener;
import wicket.resource.Properties;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.StringResourceStream;
import wicket.util.tester.TagTester;
import wicket.util.value.ValueMap;

/**
 * Test wicket:message attribute localization
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class WicketMessageAttributeTest extends WicketTestCase
{
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		tester.getApplication().getResourceSettings().setPropertiesFactory(PropertiesFactory.INSTANCE);
	}

	/**
	 * Test case where wicket:message attr is inside a wicket component
	 * 
	 * @throws Exception
	 */
	public void testInComponent() throws Exception
	{
		Page page = new TestPage()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String getMarkupString()
			{
				return "<span wicket:id='label' wicket:message='title:title-key,style:style-key'></span>";
			}

		};
		new Label(page, "label", "i am label");


		tester.startPage(page);
		TagTester tagTester = tester.getTagByWicketId("label");
		assertTrue("title-value".equals(tagTester.getAttribute("title")));
		assertTrue("style-value".equals(tagTester.getAttribute("style")));
	}

	/**
	 * Test default value fallback
	 * 
	 * @throws Exception
	 */
	public void testDefaultValue() throws Exception
	{
		Page page = new TestPage()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String getMarkupString()
			{
				return "<span wicket:id='label' width='100%' wicket:message='title:title-key,style:style-key,width:width-key'></span>";
			}

		};
		new Label(page, "label", "i am label");

		tester.startPage(page);
		TagTester tagTester = tester.getTagByWicketId("label");
		assertTrue("100%".equals(tagTester.getAttribute("width")));
	}

	/**
	 * Test wicket:message attr inside raw markup
	 * 
	 * @throws Exception
	 */
	public void testRawMarkup() throws Exception
	{
		Page page = new TestPage()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String getMarkupString()
			{
				return "<span wicket:message='title:title-key,style:style-key'/>";
			}

		};
		tester.startPage(page);
		String response = tester.getServletResponse().getDocument();
		assertTrue(response.contains("title=\"title-value\""));
		assertTrue(response.contains("style=\"style-value\""));
	}

	/**
	 * Test a wicket component inside a raw markup with wicket:message attr
	 * 
	 * @throws Exception
	 */
	public void testRawMarkupWithEmbeddedComponents() throws Exception
	{
		Page page = new TestPage()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String getMarkupString()
			{
				return "<span wicket:message='title:title-key,style:style-key'><span wicket:id='label'></span></span>";
			}

		};
		new Label(page, "label", "[[SUCCESS]]");

		tester.startPage(page);
		String response = tester.getServletResponse().getDocument();
		assertTrue(response.contains("title=\"title-value\""));
		assertTrue(response.contains("style=\"style-value\""));
		assertTrue(response.contains("[[SUCCESS]]"));
	}

	/**
	 * Base test page
	 * 
	 * @author ivaynberg
	 */
	private static abstract class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{

		protected abstract String getMarkupString();

		public final IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<? extends MarkupContainer> containerClass)
		{
			return new StringResourceStream("<html><body>" + getMarkupString() + "</body></html>");
		}

	}

	/**
	 * Properties factory for tests
	 * 
	 * @author ivaynberg
	 */
	private static class PropertiesFactory implements IPropertiesFactory
	{
		/** singleton instance */
		public static final IPropertiesFactory INSTANCE = new PropertiesFactory();

		private static Properties EMPTY = new Properties("key", new ValueMap());
		private static Properties PAGE = new Properties("key2", new ValueMap(
				"title-key=title-value,style-key=style-value"));


		public void addListener(IPropertiesReloadListener listener)
		{
			// noop
		}

		public void clearCache()
		{
			// noop
		}

		public Properties get(Class clazz, String style, Locale locale)
		{
			if (Page.class.isAssignableFrom(clazz))
			{
				return PAGE;
			}
			else
			{
				return EMPTY;
			}
		}

	}

}
