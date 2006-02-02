package wicket.examples.ajax.builtin;

import java.util.ArrayList;
import java.util.List;

import wicket.AttributeModifier;
import wicket.examples.WicketExamplePage;
import wicket.extensions.markup.html.tabs.AbstractTab;
import wicket.extensions.markup.html.tabs.AjaxTabbedPanel;
import wicket.extensions.markup.html.tabs.TabbedPanel;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;
import wicket.model.PropertyModel;

public class TabbedPanelPage extends BasePage
{
	/**
	 * Constructor
	 */
	public TabbedPanelPage()
	{

		// create a list of ITab objects used to feed the tabbed panel
		List tabs = new ArrayList();
		tabs.add(new AbstractTab(new Model("first tab"))
		{

			public Panel getPanel(String panelId)
			{
				return new TabPanel1(panelId);
			}

		});

		tabs.add(new AbstractTab(new Model("second tab"))
		{

			public Panel getPanel(String panelId)
			{
				return new TabPanel2(panelId);
			}

		});

		tabs.add(new AbstractTab(new Model("third tab"))
		{

			public Panel getPanel(String panelId)
			{
				return new TabPanel3(panelId);
			}

		});

		add(new AjaxTabbedPanel("tabs", tabs));

	}


	/**
	 * Panel representing the content panel for the first tab
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	private static class TabPanel1 extends Panel
	{

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

	};

	/**
	 * Panel representing the content panel for the second tab
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	private static class TabPanel2 extends Panel
	{

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

	};

	/**
	 * Panel representing the content panel for the third tab
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	private static class TabPanel3 extends Panel
	{

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

	};


	
}
