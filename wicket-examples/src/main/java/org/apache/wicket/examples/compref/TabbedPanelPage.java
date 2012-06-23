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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;


/**
 * Reference page for TabbedPanel wicket-extensions component
 * 
 * @see org.apache.wicket.extensions.markup.html.tabs.TabbedPanel
 * 
 * @author igor
 * 
 */
public class TabbedPanelPage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public TabbedPanelPage()
	{
		setDefaultModel(new Model<String>("tabpanel"));

		// create links used to switch between css variations
		addCssSwitchingLinks();

		// create a list of ITab objects used to feed the tabbed panel
		final List<ITab> tabs = new ArrayList<ITab>();
		tabs.add(new AbstractTab(new Model<String>("first tab"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Panel getPanel(String panelId)
			{
				return new TabPanel1(panelId);
			}

		});

		tabs.add(new AbstractTab(new Model<String>("second tab"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Panel getPanel(String panelId)
			{
				return new TabPanel2(panelId);
			}

		});

		tabs.add(new AbstractTab(new Model<String>("third tab"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Panel getPanel(String panelId)
			{
				return new TabPanel3(panelId);
			}

		});

		// add the new tabbed panel, attribute modifier only used to switch
		// between different css variations
		final TabbedPanel<ITab> tabbedPanel = new TabbedPanel<ITab>("tabs", tabs);
		tabbedPanel.add(AttributeModifier.replace("class", TabbedPanelPage.this.getDefaultModel()));
		add(tabbedPanel);

		add(new Link<Void>("skip")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				int index = tabbedPanel.getModelObject();

				tabbedPanel.setModelObject((index + 1) % tabs.size());
			}
		});
	}

	private void addCssSwitchingLinks()
	{
		add(new CssSwitchingLink("var0", "tabpanel"));
		add(new CssSwitchingLink("var1", "tabpanel1"));
		add(new CssSwitchingLink("var2", "tabpanel2"));
		add(new CssSwitchingLink("var3", "tabpanel3"));
		add(new CssSwitchingLink("var4", "tabpanel4"));
	}

	protected class CssSwitchingLink extends Link<Void>
	{
		private static final long serialVersionUID = 1L;

		private final String clazz;

		/**
		 * @param id
		 * @param clazz
		 */
		public CssSwitchingLink(String id, String clazz)
		{
			super(id);
			this.clazz = clazz;
		}

		/**
		 * @see org.apache.wicket.markup.html.link.Link#onClick()
		 */
		@Override
		public void onClick()
		{
			TabbedPanelPage.this.setDefaultModelObject(clazz);
		}

		/**
		 * @see org.apache.wicket.markup.html.link.Link#isEnabled()
		 */
		@Override
		public boolean isEnabled()
		{
			return !TabbedPanelPage.this.getDefaultModelObjectAsString().equals(clazz);
		}

	}

	/**
	 * Panel representing the content panel for the first tab
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	private static class TabPanel1 extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * 
		 * @param id
		 *            component id
		 */
		public TabPanel1(String id)
		{
			super(id);
		}

	}

	/**
	 * Panel representing the content panel for the second tab
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	private static class TabPanel2 extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * 
		 * @param id
		 *            component id
		 */
		public TabPanel2(String id)
		{
			super(id);
		}

	}

	/**
	 * Panel representing the content panel for the third tab
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	private static class TabPanel3 extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * 
		 * @param id
		 *            component id
		 */
		public TabPanel3(String id)
		{
			super(id);
		}

	}

	@Override
	protected void explain()
	{
		String html = "<span wicket:id=\"tabs\" class=\"tabpanel\">[tabbed panel will be here]</span>\n";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;List tabs=new ArrayList();<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;tabs.add(new AbstractTab(new Model&lt;String&gt;(\"first tab\")) {<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;public Panel getPanel(String panelId) { return new TabPanel1(panelId); }<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;});<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;tabs.add(new AbstractTab(new Model&lt;String&gt;(\"second tab\")) {<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;public Panel getPanel(String panelId) { return new TabPanel2(panelId); }<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;});<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;tabs.add(new AbstractTab(new Model&lt;String&gt;(\"third tab\")) {<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;public Panel getPanel(String panelId) { return new TabPanel3(panelId); }<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;});<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;add(new TabbedPanel(\"tabs\", tabs)<br/>";
		add(new ExplainPanel(html, code));
	}
}
