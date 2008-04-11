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
package org.apache.wicket.extensions.markup.html.tabs;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.Loop.LoopItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * TabbedPanel component represets a panel with tabs that are used to switch between different
 * content panels inside the TabbedPanel panel.
 * 
 * <p>
 * <b>Note:</b> When the currently selected tab is replaced by changing the underlying list of
 * tabs, the change is not picked up unless a call is made to {@link #setSelectedTab(int)}.
 * <p>
 * 
 * Example:
 * 
 * <pre>
 * 
 * List tabs=new ArrayList();
 * 
 * tabs.add(new AbstractTab(new Model(&quot;first tab&quot;)) {
 * 
 *   public Panel getPanel(String panelId)
 *   {
 *     return new TabPanel1(panelId);
 *   }
 * 
 * });
 * 
 * tabs.add(new AbstractTab(new Model(&quot;second tab&quot;)) {
 * 
 *   public Panel getPanel(String panelId)
 *   {
 *     return new TabPanel2(panelId);
 *   }
 * 
 * });
 * 
 * add(new TabbedPanel(&quot;tabs&quot;, tabs));
 * 
 * 
 * &lt;span wicket:id=&quot;tabs&quot; class=&quot;tabpanel&quot;&gt;[tabbed panel will be here]&lt;/span&gt;
 * 
 * 
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * For a complete example see the component references in wicket-examples project
 * </p>
 * 
 * @see org.apache.wicket.extensions.markup.html.tabs.ITab
 * 
 * @author Igor Vaynberg (ivaynberg at apache dot org)
 * 
 */
public class TabbedPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * id used for child panels
	 */
	public static final String TAB_PANEL_ID = "panel";


	private final List tabs;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param tabs
	 *            list of ITab objects used to represent tabs
	 */
	public TabbedPanel(String id, List tabs)
	{
		super(id, new Model(new Integer(-1)));

		if (tabs == null)
		{
			throw new IllegalArgumentException("argument [tabs] cannot be null");
		}

		this.tabs = tabs;

		final IModel tabCount = new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject()
			{
				return new Integer(TabbedPanel.this.tabs.size());
			}
		};

		WebMarkupContainer tabsContainer = new WebMarkupContainer("tabs-container")
		{
			private static final long serialVersionUID = 1L;

			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.put("class", getTabContainerCssClass());
			}
		};
		add(tabsContainer);

		// add the loop used to generate tab names
		tabsContainer.add(new Loop("tabs", tabCount)
		{
			private static final long serialVersionUID = 1L;

			protected void populateItem(LoopItem item)
			{
				final int index = item.getIteration();
				final ITab tab = ((ITab)TabbedPanel.this.tabs.get(index));

				final WebMarkupContainer titleLink = newLink("link", index);

				titleLink.add(newTitle("title", tab.getTitle(), index));
				item.add(titleLink);
			}

			protected LoopItem newItem(int iteration)
			{
				return newTabContainer(iteration);
			}

		});
	}


	/**
	 * Generates a loop item used to represent a specific tab's <code>li</code> element.
	 * 
	 * @param tabIndex
	 * @return new loop item
	 */
	protected LoopItem newTabContainer(int tabIndex)
	{
		return new LoopItem(tabIndex)
		{
			private static final long serialVersionUID = 1L;

			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				String cssClass = (String)tag.getString("class");
				if (cssClass == null)
				{
					cssClass = " ";
				}
				cssClass += " tab" + getIteration();

				if (getIteration() == getSelectedTab())
				{
					cssClass += " selected";
				}
				if (getIteration() == getTabs().size() - 1)
				{
					cssClass += " last";
				}
				tag.put("class", cssClass.trim());
			}

		};
	}


	// @see org.apache.wicket.Component#onAttach()
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		if (!hasBeenRendered() && getSelectedTab() == -1)
		{
			// select the first tab by default
			setSelectedTab(0);
		}
	}

	/**
	 * @return the value of css class attribute that will be added to a div containing the tabs. The
	 *         default value is <code>tab-row</code>
	 */
	protected String getTabContainerCssClass()
	{
		return "tab-row";
	}

	/**
	 * @return list of tabs that can be used by the user to add/remove/reorder tabs in the panel
	 */
	public final List getTabs()
	{
		return tabs;
	}

	/**
	 * Factory method for tab titles. Returned component can be anything that can attach to span
	 * tags such as a fragment, panel, or a label
	 * 
	 * @param titleId
	 *            id of title component
	 * @param titleModel
	 *            model containing tab title
	 * @param index
	 *            index of tab
	 * @return title component
	 */
	protected Component newTitle(String titleId, IModel titleModel, int index)
	{
		return new Label(titleId, titleModel);
	}


	/**
	 * Factory method for links used to switch between tabs.
	 * 
	 * The created component is attached to the following markup. Label component with id: title
	 * will be added for you by the tabbed panel.
	 * 
	 * <pre>
	 *            &lt;a href=&quot;#&quot; wicket:id=&quot;link&quot;&gt;&lt;span wicket:id=&quot;title&quot;&gt;[[tab title]]&lt;/span&gt;&lt;/a&gt;
	 * </pre>
	 * 
	 * Example implementation:
	 * 
	 * <pre>
	 * protected WebMarkupContainer newLink(String linkId, final int index)
	 * {
	 * 	return new Link(linkId)
	 * 	{
	 * 		private static final long serialVersionUID = 1L;
	 * 
	 * 		public void onClick()
	 * 		{
	 * 			setSelectedTab(index);
	 * 		}
	 * 	};
	 * }
	 * </pre>
	 * 
	 * @param linkId
	 *            component id with which the link should be created
	 * @param index
	 *            index of the tab that should be activated when this link is clicked. See
	 *            {@link #setSelectedTab(int)}.
	 * @return created link component
	 */
	protected WebMarkupContainer newLink(String linkId, final int index)
	{
		return new Link(linkId)
		{
			private static final long serialVersionUID = 1L;

			public void onClick()
			{
				setSelectedTab(index);
			}
		};
	}

	/**
	 * sets the selected tab
	 * 
	 * @param index
	 *            index of the tab to select
	 * 
	 */
	public void setSelectedTab(int index)
	{
		if (index < 0 || index >= tabs.size())
		{
			throw new IndexOutOfBoundsException();
		}

		setModelObject(new Integer(index));

		ITab tab = (ITab)tabs.get(index);

		Panel panel = tab.getPanel(TAB_PANEL_ID);

		if (panel == null)
		{
			throw new WicketRuntimeException("ITab.getPanel() returned null. TabbedPanel [" +
				getPath() + "] ITab index [" + index + "]");

		}

		if (!panel.getId().equals(TAB_PANEL_ID))
		{
			throw new WicketRuntimeException(
				"ITab.getPanel() returned a panel with invalid id [" +
					panel.getId() +
					"]. You must always return a panel with id equal to the provided panelId parameter. TabbedPanel [" +
					getPath() + "] ITab index [" + index + "]");
		}


		if (get(TAB_PANEL_ID) == null)
		{
			add(panel);
		}
		else
		{
			replace(panel);
		}
	}

	/**
	 * @return index of the selected tab
	 */
	public final int getSelectedTab()
	{
		return ((Integer)getModelObject()).intValue();
	}

}
