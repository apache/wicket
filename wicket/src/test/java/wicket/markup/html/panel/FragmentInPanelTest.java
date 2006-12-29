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
package wicket.markup.html.panel;

import wicket.MarkupContainer;
import wicket.WicketTestCase;
import wicket.markup.IMarkupResourceStreamProvider;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.model.Model;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.StringResourceStream;

/**
 * Test markup resolution of children components of a fragment that is embedded
 * in a panel
 * 
 * @author ivaynberg
 */
public class FragmentInPanelTest extends WicketTestCase
{
	/**
	 * Test markup resolution of children components of a fragment that is
	 * embedded in a panel
	 * 
	 * @throws Exception
	 */
	public void testFragmentInPanel() throws Exception
	{
		assertTrue(accessPage(TestPage.class).getDocument().contains("[[SUCCESS]]"));
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
			new TestPanel(this, "panel");
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<? extends MarkupContainer> containerClass)
		{
			return new StringResourceStream(
					"<html><body><span wicket:id='panel'></span></body></html>");
		}
	}

	/**
	 * Test panel
	 * 
	 * @author ivaynberg
	 */
	public static class TestPanel extends Panel implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param id
		 */
		public TestPanel(MarkupContainer parent, String id)
		{
			super(parent, id);
			
			Fragment fragment = new Fragment(this, "fragment", "frag-id", TestPanel.this);
			new Label(fragment, "label", new Model<String>("[[SUCCESS]]"));
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<? extends MarkupContainer> containerClass)
		{
			return new StringResourceStream(
					"<wicket:panel><span wicket:id='fragment'></span></wicket:panel>"
							+ "<wicket:fragment wicket:id='frag-id'><span wicket:id='label'></span></wicket:fragment>");
		}
	}
}
