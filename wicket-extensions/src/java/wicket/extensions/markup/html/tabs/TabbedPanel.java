/*
 * $Id: TabbedPanel.java 4079 2006-02-02 10:18:54 -0800 (Thu, 02 Feb 2006)
 * ivaynberg $ $Revision$ $Date: 2006-02-02 10:18:54 -0800 (Thu, 02 Feb
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.tabs;

import java.util.List;

import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.behavior.AttributeAppender;
import wicket.behavior.SimpleAttributeModifier;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.Loop;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

/**
 * TabbedPanel component represets a panel with tabs that are used to switch
 * between different content panels inside the TabbedPanel panel.
 * <p>
 * Example:
 * 
 * <pre>
 *                           
 *                           List tabs=new ArrayList();
 *                           
 *                           tabs.add(new AbstractTab(new Model(&quot;first tab&quot;)) {
 *                          
 *                           public Panel getPanel(String panelId)
 *                           {
 *                           return new TabPanel1(panelId);
 *                           }
 *                           
 *                           });
 *                          
 *                           tabs.add(new AbstractTab(new Model(&quot;second tab&quot;)) {
 *                          
 *                           public Panel getPanel(String panelId)
 *                           {
 *                           return new TabPanel2(panelId);
 *                           }
 *                           
 *                           });
 *                          
 *                           add(new TabbedPanel(&quot;tabs&quot;, tabs);
 *                       
 *                           
 *                           &lt;span wicket:id=&quot;tabs&quot; class=&quot;tabpanel&quot;&gt;[tabbed panel will be here]&lt;/span&gt;
 *                       
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * For a complete example see the component references in wicket-examples
 * project
 * </p>
 * 
 * @see wicket.extensions.markup.html.tabs.ITab
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class TabbedPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * id used for child panels
	 */
	public static final String TAB_PANEL_ID = "panel";


	private List tabs;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param tabs
	 *            list of ITab objects used to represent tabs
	 */
	public TabbedPanel(MarkupContainer parent, final String id, List tabs)
	{
		super(parent, id, new Model(new Integer(-1)));

		if (tabs == null)
		{
			throw new IllegalArgumentException("argument [tabs] cannot be null");
		}

		if (tabs.size() < 1)
		{
			throw new IllegalArgumentException(
					"argument [tabs] must contain a list of at least one tab");
		}

		this.tabs = tabs;

		// add the loop used to generate tab names
		new Loop(this, "tabs", tabs.size())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(LoopItem item)
			{
				final int index = item.getIteration();
				final ITab tab = ((ITab)TabbedPanel.this.tabs.get(index));
				final int selected = getSelectedTab();

				final WebMarkupContainer titleLink = newLink(item, "link", index);

				new Label(titleLink, "title", tab.getTitle());

				item.add(new SimpleAttributeModifier("class", "selected")
				{
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isEnabled()
					{
						return index == selected;
					}

				});
				if (item.getIteration() == getIterations() - 1)
				{
					item.add(new AttributeAppender("class", true, new Model("last"), " "));
				}

			}

		};

		// select the first tab by default
		setSelectedTab(0);

	}

	/**
	 * Factory method for links used to switch between tabs.
	 * 
	 * The created component is attached to the following markup. Label
	 * component with id: title will be added for you by the tabbed panel.
	 * 
	 * <pre>
	 *      &lt;a href=&quot;#&quot; wicket:id=&quot;link&quot;&gt;&lt;span wicket:id=&quot;title&quot;&gt;[[tab title]]&lt;/span&gt;&lt;/a&gt;
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
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param linkId
	 *            component id with which the link should be created
	 * @param index
	 *            index of the tab that should be activated when this link is
	 *            clicked. See {@link #setSelectedTab(int)}.
	 * @return created link component
	 */
	protected WebMarkupContainer newLink(MarkupContainer parent, String linkId, final int index)
	{
		return new Link(parent, linkId)
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
	 * 
	 */
	public final void setSelectedTab(int index)
	{
		if (index < 0 || index >= tabs.size())
		{
			throw new IndexOutOfBoundsException();
		}

		setModelObject(new Integer(index));

		ITab tab = (ITab)tabs.get(index);

		Panel panel = tab.getPanel(this, TAB_PANEL_ID);

		if (panel == null)
		{
			throw new WicketRuntimeException("ITab.getPanel() returned null. TabbedPanel ["
					+ getPath() + "] ITab index [" + index + "]");

		}

		if (!panel.getId().equals(TAB_PANEL_ID))
		{
			throw new WicketRuntimeException(
					"ITab.getPanel() returned a panel with invalid id ["
							+ panel.getId()
							+ "]. You must always return a panel with id equal to the provided panelId parameter. TabbedPanel ["
							+ getPath() + "] ITab index [" + index + "]");
		}

		panel.reAttach();
	}

	/**
	 * @return index of the selected tab
	 */
	public final int getSelectedTab()
	{
		return ((Integer)getModelObject()).intValue();
	}

}
