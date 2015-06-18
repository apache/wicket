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
package org.apache.wicket;

import org.apache.wicket.ajax.IAjaxRegionMarkupIdProvider;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for <a href="https://issues.apache.org/jira/browse/WICKET-3563">WICKET-3563</a>
 */
public class PlaceholderTagIdTest extends WicketTestCase
{

	/**
	 * If a component either implements {@link IAjaxRegionMarkupIdProvider} or has a behavior which
	 * implements it then the placeholder markup id for it should be the one defined by the
	 * {@link IAjaxRegionMarkupIdProvider}
	 */
	@Test
	public void wicket3563()
	{
		tester.startPage(TestPage.class);

		tester.assertContains("<form id=\"form1_region\" style=\"display:none\">");
	}

	/**
	 *
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public TestPage()
		{
			Form<?> form = new Form<Void>("form");
			form.setVisible(false);
			form.setOutputMarkupPlaceholderTag(true);
			add(form);

			form.add(new RegionBehavior());
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><form wicket:id='form'></form></body></html>");
		}
	}

	/**
	 * A behavior that implements {@link IAjaxRegionMarkupIdProvider}
	 */
	public static class RegionBehavior extends Behavior implements IAjaxRegionMarkupIdProvider
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String getAjaxRegionMarkupId(Component component)
		{
			return component.getMarkupId() + "_region";
		}

	}
}
