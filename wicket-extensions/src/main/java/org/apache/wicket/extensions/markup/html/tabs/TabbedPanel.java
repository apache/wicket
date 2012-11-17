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
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;


/**
 * TabbedPanel component represents a panel with tabs that are used to switch between different
 * content panels inside the TabbedPanel panel.
 * <p>
 * <b>Note:</b> When the currently selected tab is replaced by changing the underlying list of tabs,
 * the change is not picked up unless a call is made to {@link #setSelectedTab(int)}.
 * <p>
 * Example:
 * 
 * <pre>
 * List tabs=new ArrayList();
 * tabs.add(new AbstractTab(new Model&lt;String&gt;(&quot;first tab&quot;)) {
 *   public Panel getPanel(String panelId)
 *   {
 *     return new TabPanel1(panelId);
 *   }
 * });
 * 
 * tabs.add(new AbstractTab(new Model&lt;String&gt;(&quot;second tab&quot;)) {
 *   public Panel getPanel(String panelId)
 *   {
 *     return new TabPanel2(panelId);
 *   }
 * });
 * 
 * add(new TabbedPanel(&quot;tabs&quot;, tabs));
 * 
 * &lt;span wicket:id=&quot;tabs&quot; class=&quot;tabpanel&quot;&gt;[tabbed panel will be here]&lt;/span&gt;
 * </pre>
 * <p>
 * For a complete example see the component references in wicket-examples project
 * 
 * @see org.apache.wicket.extensions.markup.html.tabs.ITab
 * 
 * @author Igor Vaynberg (ivaynberg at apache dot org)
 * @param <T>
 *            The type of panel to be used for this component's tabs. Just use {@link ITab} if you
 *            have no special needs here.
 */
public class TabbedPanel<T extends ITab> extends Panel
{
	private static final long serialVersionUID = 1L;

	/** id used for child panels */
	public static final String TAB_PANEL_ID = "panel";

	private final List<T> tabs;

	/** the current tab */
	private int currentTab = -1;

	private transient Boolean[] tabsVisibilityCache;
    private transient int lastVisibleTabIndex;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param tabs
	 *            list of ITab objects used to represent tabs
	 */
	public TabbedPanel(final String id, final List<T> tabs)
	{
		this(id, tabs, null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param tabs
	 *            list of ITab objects used to represent tabs
	 * @param model
	 *            model holding the index of the selected tab
	 */
	public TabbedPanel(final String id, final List<T> tabs, IModel<Integer> model)
	{
		super(id, model);

		this.tabs = Args.notNull(tabs, "tabs");

		final IModel<Integer> tabCount = new AbstractReadOnlyModel<Integer>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Integer getObject()
			{
				return TabbedPanel.this.tabs.size();
			}
		};

		WebMarkupContainer tabsContainer = newTabsContainer("tabs-container");
		add(tabsContainer);

		// add the loop used to generate tab names
		tabsContainer.add(new Loop("tabs", tabCount)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final LoopItem item)
			{
				final int index = item.getIndex();
				final T tab = TabbedPanel.this.tabs.get(index);

				final WebMarkupContainer titleLink = newLink("link", index);

				titleLink.add(newTitle("title", tab.getTitle(), index));
				item.add(titleLink);
			}

			@Override
			protected LoopItem newItem(final int iteration)
			{
				return newTabContainer(iteration);
			}
		});

		add(newPanel());
	}

	/**
	 * Override of the default initModel behaviour. This component <strong>will not</strong> use any
	 * compound model of a parent.
	 * 
	 * @see org.apache.wicket.Component#initModel()
	 */
	@Override
	protected IModel<?> initModel()
	{
		return new Model<Integer>(-1);
	}

	/**
	 * Generates the container for all tabs. The default container automatically adds the css
	 * <code>class</code> attribute based on the return value of {@link #getTabContainerCssClass()}
	 * 
	 * @param id
	 *            container id
	 * @return container
	 */
	protected WebMarkupContainer newTabsContainer(final String id)
	{
		return new WebMarkupContainer(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(final ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.put("class", getTabContainerCssClass());
			}
		};
	}

	/**
	 * Generates a loop item used to represent a specific tab's <code>li</code> element.
	 * 
	 * @param tabIndex
	 * @return new loop item
	 */
	protected LoopItem newTabContainer(final int tabIndex)
	{
		return new LoopItem(tabIndex)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(final ComponentTag tag)
			{
				super.onComponentTag(tag);
				String cssClass = tag.getAttribute("class");
				if (cssClass == null)
				{
					cssClass = " ";
				}
				cssClass += " tab" + getIndex();

				if (getIndex() == getSelectedTab())
				{
					cssClass += ' ' + getSelectedTabCssClass();
				}
				if (getIndex() == lastVisibleTabIndex)
				{
					cssClass += ' ' + getLastTabCssClass();
				}
				tag.put("class", cssClass.trim());
			}

			@Override
			public boolean isVisible()
			{
				return getTabs().get(tabIndex).isVisible();
			}
		};
	}

	@Override
	protected void onBeforeRender()
	{
		int index = getSelectedTab();

		if ((index == -1) || (isTabVisible(index) == false))
		{
			// find first visible tab
			index = -1;
			for (int i = 0; i < tabs.size(); i++)
			{
				if (isTabVisible(i))
				{
					index = i;
					break;
				}
			}

			if (index != -1)
			{
				/*
				 * found a visible tab, so select it
				 */
				setSelectedTab(index);
			}
		}

        lastVisibleTabIndex = -1;
        for (int i = 0; i < tabs.size(); i++)
        {
            if (isTabVisible(i))
            {
                lastVisibleTabIndex = i;
            }
        }

        setCurrentTab(index);

		super.onBeforeRender();
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
	 * @return the value of css class attribute that will be added to last tab. The default value is
	 *         <code>last</code>
	 */
	protected String getLastTabCssClass()
	{
		return "last";
	}

	/**
	 * @return the value of css class attribute that will be added to selected tab. The default
	 *         value is <code>selected</code>
	 */
	protected String getSelectedTabCssClass()
	{
		return "selected";
	}

	/**
	 * @return list of tabs that can be used by the user to add/remove/reorder tabs in the panel
	 */
	public final List<T> getTabs()
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
	protected Component newTitle(final String titleId, final IModel<?> titleModel, final int index)
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
	 * &lt;a href=&quot;#&quot; wicket:id=&quot;link&quot;&gt;&lt;span wicket:id=&quot;title&quot;&gt;[[tab title]]&lt;/span&gt;&lt;/a&gt;
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
	protected WebMarkupContainer newLink(final String linkId, final int index)
	{
		return new Link<Void>(linkId)
		{
			private static final long serialVersionUID = 1L;

			@Override
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
	 * @return this for chaining
	 * @throws IndexOutOfBoundsException
	 *             if index is not {@code -1} or in the range of available tabs
	 */
	public TabbedPanel<T> setSelectedTab(final int index)
	{
		if ((index < 0) || (index >= tabs.size()))
		{
			throw new IndexOutOfBoundsException();
		}

		setDefaultModelObject(index);

		// force the tab's component to be aquired again if already the current tab
		currentTab = -1;
		setCurrentTab(index);

		return this;
	}

	private void setCurrentTab(int index)
	{
		if (this.currentTab == index)
		{
			// already current
			return;
		}
		this.currentTab = index;

		final Component component;

		if (currentTab == -1 || (tabs.size() == 0) || !isTabVisible(currentTab))
		{
			// no tabs or the current tab is not visible
			component = newPanel();
		}
		else
		{
			// show panel from selected tab
			T tab = tabs.get(currentTab);
			component = tab.getPanel(TAB_PANEL_ID);
			if (component == null)
			{
				throw new WicketRuntimeException("ITab.getPanel() returned null. TabbedPanel [" +
					getPath() + "] ITab index [" + currentTab + "]");
			}
		}

		if (!component.getId().equals(TAB_PANEL_ID))
		{
			throw new WicketRuntimeException(
				"ITab.getPanel() returned a panel with invalid id [" +
					component.getId() +
					"]. You must always return a panel with id equal to the provided panelId parameter. TabbedPanel [" +
					getPath() + "] ITab index [" + currentTab + "]");
		}

		addOrReplace(component);
	}

	private WebMarkupContainer newPanel()
	{
		return new WebMarkupContainer(TAB_PANEL_ID);
	}

	/**
	 * @return index of the selected tab
	 */
	public final int getSelectedTab()
	{
		return (Integer)getDefaultModelObject();
	}

	/**
	 * 
	 * @param tabIndex
	 * @return visible
	 */
	private boolean isTabVisible(final int tabIndex)
	{
		if (tabsVisibilityCache == null)
		{
			tabsVisibilityCache = new Boolean[tabs.size()];
		}

		if (tabsVisibilityCache.length < tabIndex + 1)
		{
			Boolean[] resized = new Boolean[tabIndex + 1];
			System.arraycopy(tabsVisibilityCache, 0, resized, 0, tabsVisibilityCache.length);
			tabsVisibilityCache = resized;
		}

		if (tabsVisibilityCache.length > 0)
		{
			Boolean visible = tabsVisibilityCache[tabIndex];
			if (visible == null)
			{
				visible = tabs.get(tabIndex).isVisible();
				tabsVisibilityCache[tabIndex] = visible;
			}
			return visible;
		}
		else
		{
			return false;
		}
	}

	@Override
	protected void onDetach()
	{
		tabsVisibilityCache = null;
		super.onDetach();
	}
}
