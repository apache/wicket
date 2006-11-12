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
package wicket.markup.html.border;

import wicket.MarkupContainer;
import wicket.WicketTestCase;
import wicket.markup.IAlternateParentProvider;
import wicket.markup.IMarkupResourceStreamProvider;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.StringResourceStream;

/**
 * Test borders where wicket:body is a child of border's child instead of a
 * direct child of the border
 * 
 * @author ivaynberg
 */
public class WrappedWicketBodyTest extends WicketTestCase
{
	/**
	 * Test borders where wicket:body is a child of border's child instead of a
	 * direct child of the border
	 * 
	 * @throws Exception
	 */
	public void testSimpleBorder() throws Exception
	{
		String document = accessPage(TestPage3.class).getDocument();
		assertTrue(document.contains("[[SUCCESS]]"));
		assertTrue(document.contains("[[TEST]]"));
	}

	/**
	 * Test borders where wicket:body is a child of border's child instead of a
	 * direct child of the border
	 * 
	 * @throws Exception
	 */
	public void testWicketBodyContainer() throws Exception
	{
		String document = accessPage(TestPage.class).getDocument();
		assertTrue(document.contains("[[SUCCESS]]"));
		assertTrue(document.contains("[[TEST]]"));
		assertTrue(document.contains("[[TEST-2]]"));
	}

	/**
	 * Same as {@link #testWicketBodyContainer()}, but tests if borders operate
	 * properly when embedded in each other
	 * 
	 * @throws Exception
	 */
	public void testMultiLevelWicketBodyContainer() throws Exception
	{
		// FIXME I wasn't able to make this work (JDo 2006-09-28); need to make
		// some other changes first
		String document = accessPage(TestPage2.class).getDocument();
		assertTrue(document.contains("[[SUCCESS]]"));
		assertTrue(document.contains("[[TEST]]"));
		assertTrue(document.contains("[[TEST-2]]"));
	}

	/**
	 * Test page
	 * 
	 * @author ivaynberg
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public TestPage()
		{
			Border border = new TestBorder(this, "border");
			WebMarkupContainer container = new WebMarkupContainer(border, "container");
			new Label(container, "label", "[[SUCCESS]]");
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<? extends MarkupContainer> containerClass)
		{
			return new StringResourceStream(
					"<html><body><span wicket:id='border'><span wicket:id='container'><span wicket:id='label'></span></span></span></body></html>");
		}
	}

	/**
	 * Test page
	 * 
	 * @author ivaynberg
	 */
	public static class TestPage2 extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public TestPage2()
		{
			Border border = new TestBorder(this, "border");
			Border border2 = new TestBorder(border, "border2");
			new Label(border2, "label", "[[SUCCESS]]");
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<? extends MarkupContainer> containerClass)
		{
			return new StringResourceStream(
					"<html><body><span wicket:id='border'><span wicket:id='border2'><span wicket:id='label'></span></span></span></body></html>");
		}
	}

	/**
	 * Test page
	 * 
	 * @author ivaynberg
	 */
	public static class TestPage3 extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public TestPage3()
		{
			Border border = new TestBorder2(this, "border");
			new Label(border, "label", "[[SUCCESS]]");
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<? extends MarkupContainer> containerClass)
		{
			return new StringResourceStream(
					"<html><body><span wicket:id='border'><span wicket:id='label'></span></span></body></html>");
		}
	}

	/**
	 * Test border that implemetns {@link IAlternateParentProvider}
	 * 
	 * @author ivaynberg
	 */
	public static class TestBorder extends Border
			implements
				IMarkupResourceStreamProvider,
				IAlternateParentProvider
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param id
		 */
		public TestBorder(MarkupContainer parent, String id)
		{
			super(parent, id);
			MarkupContainer bodyParent = new WebMarkupContainer(this, "body-parent");
			setBorderBodyContainer(bodyParent);

			new Label(this, "borderLabel", "[[TEST]]");
			new Label(bodyParent, "borderLabel2", "[[TEST-2]]");
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<? extends MarkupContainer> containerClass)
		{
			return new StringResourceStream(
					"<wicket:border><span wicket:id='body-parent'><wicket:body/><span wicket:id='borderLabel2'></span></span><span wicket:id='borderLabel'></span></wicket:border>");
		}

		/**
		 * 
		 * @see wicket.markup.IAlternateParentProvider#getAlternateParent(java.lang.Class,
		 *      java.lang.String)
		 */
		public MarkupContainer getAlternateParent(Class childClass, String childId)
		{
			return (getBodyContainer() != null && !"borderLabel".equals(childId) 
					? getBodyContainer() : this);
		}
	}

	/**
	 * Test border that implemetns {@link IAlternateParentProvider}
	 * 
	 * @author ivaynberg
	 */
	public static class TestBorder2 extends Border implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param id
		 */
		public TestBorder2(MarkupContainer parent, String id)
		{
			super(parent, id);
			new Label(this, "borderLabel", "[[TEST]]");
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<? extends MarkupContainer> containerClass)
		{
			return new StringResourceStream(
					"<wicket:border><span wicket:id='borderLabel'></span><wicket:body/></wicket:border>");
		}
	}
}
