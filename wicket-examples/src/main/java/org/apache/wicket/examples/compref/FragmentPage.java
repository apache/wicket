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
package org.apache.wicket.examples.compref;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;


/**
 * Page with examples on {@link org.apache.wicket.markup.html.panel.Fragment}.
 * 
 * @author Eelco Hillenius
 */
public class FragmentPage extends WicketExamplePage
{
	/**
	 * A fragment,
	 */
	private class MyFragment extends Fragment
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 *            The component Id
		 * @param markupId
		 *            The id in the markup
		 * @param markupProvider
		 *            The markup provider
		 */
		public MyFragment(String id, String markupId, MarkupContainer markupProvider)
		{
			super(id, markupId, markupProvider);
			add(new Label("label", "yep, this is from a component proper"));
			add(new AnotherPanel("otherPanel"));
		}
	}

	/**
	 * Constructor
	 */
	public FragmentPage()
	{
		add(new MyFragment("fragment", "fragmentid", this));
	}

	@Override
	protected void explain()
	{
		String html = "<wicket:fragment wicket:id=\"fragmentid\">...</wicket:fragment>";
		String code = "private class MyFragment extends Fragment {\n ...\n"
			+ "add(new MyFragment(\"fragment\", \"fragmentid\"));";
		add(new ExplainPanel(html, code));
	}
}