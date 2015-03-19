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
package org.apache.wicket.resource.loader;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Test;

/**
 * test hierarchic lookup of properties from component property files
 * 
 * @author Peter Ertl
 * 
 */
public class PropertiesResolverTest extends Assert
{
	/**
	 * 
	 */
	@Test
	public void resolveProperties()
	{
		WicketTester tester = new WicketTester(new App());

		// all the tests are performed in page
		tester.startPage(MyPage.class);
		tester.assertRenderedPage(MyPage.class);
	}

	/**
	 */
	public static class App extends WebApplication
	{
		@Override
		public Class<? extends Page> getHomePage()
		{
			return MyPage.class;
		}
	}

	/**
	 */
	public static class MyPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public MyPage()
		{
			MyRepeatingView repeater = new MyRepeatingView("repeater");
			add(repeater);

			MyContainer container1 = new MyContainer(repeater.newChildId());
			repeater.add(container1);

			MyLabel label1 = new MyLabel("label");
			container1.add(label1);

			MyContainer container2 = new MyContainer(repeater.newChildId());
			repeater.add(container2);

			MyLabel label2 = new MyLabel("label");
			container2.add(label2);

			// test lookup on label properties
			assertEquals("label-1", lookup("keyLabel", label1));
			assertEquals("label-1", lookup("keyLabel", label2));

			// test lookup on container properties
			assertEquals("container-1", lookup("label.keyContainer", container1));
			assertEquals("container-2", lookup("keyContainer", container1));
			assertEquals("container-1", lookup("keyContainer", label1));
			assertEquals("container-1", lookup("label.keyContainer", container2));
			assertEquals("container-2", lookup("keyContainer", container2));
			assertEquals("container-1", lookup("keyContainer", label2));

			// test lookup on repeater properties
			assertEquals("repeater-1", lookup("label.keyRepeater", repeater));
			assertEquals("repeater-2", lookup("keyRepeater", repeater));
			assertEquals("repeater-1", lookup("label.keyRepeater", container1));
			assertEquals("repeater-2", lookup("keyRepeater", container1));
			assertEquals("repeater-1", lookup("keyRepeater", label1));
			assertEquals("repeater-1", lookup("label.keyRepeater", container2));
			assertEquals("repeater-2", lookup("keyRepeater", container2));
			assertEquals("repeater-1", lookup("keyRepeater", label2));

			// test lookup on page properties
			assertEquals("page-1", lookup("repeater.label.keyPage", MyPage.this));
			assertEquals("page-2", lookup("label.keyPage", MyPage.this));
			assertEquals("page-3", lookup("keyPage", MyPage.this));
			assertEquals("page-1", lookup("label.keyPage", repeater));
			assertEquals("page-1", lookup("label.keyPage", container1));
			assertEquals("page-1", lookup("keyPage", label1));
			assertEquals("page-1", lookup("label.keyPage", container2));
			assertEquals("page-1", lookup("keyPage", label2));
		}

		private String lookup(String key, Component anchor)
		{
			return new StringResourceModel(key, anchor, null, (String)null).getString();
		}
	}

	public static class MyRepeatingView extends RepeatingView
	{
		public MyRepeatingView(String id)
		{
			super(id);
		}
	}

	public static class MyContainer extends WebMarkupContainer
	{
		public MyContainer(String id)
		{
			super(id);
		}
	}

	public static class MyLabel extends Label
	{
		public MyLabel(String id)
		{
			super(id);
		}
	}
}
