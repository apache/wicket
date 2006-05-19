package wicket.examples.compref;

import java.util.ArrayList;
import java.util.List;

import wicket.AttributeModifier;
import wicket.examples.WicketExamplePage;
import wicket.extensions.markup.html.tabs.AbstractTab;
import wicket.extensions.markup.html.tabs.TabbedPanel;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

/**
 * Reference page for TabbedPanel wicket-extensions component
 * 
 * @see wicket.extensions.markup.html.tabs.TabbedPanel
 * 
 * @author igor
 * 
 */
public class TabbedPanelPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public TabbedPanelPage()
	{
		setModel(new Model("tabpanel"));

		// create links used to switch between css variations
		addCssSwitchingLinks();

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

		// add the new tabbed panel, attribute modifier only used to switch
		// between different css variations
		add(new TabbedPanel("tabs", tabs).add(new AttributeModifier("class", true,
				TabbedPanelPage.this.getModel())));

	}

	private void addCssSwitchingLinks()
	{
		add(new CssSwitchingLink("var0", "tabpanel"));
		add(new CssSwitchingLink("var1", "tabpanel1"));
		add(new CssSwitchingLink("var2", "tabpanel2"));
		add(new CssSwitchingLink("var3", "tabpanel3"));
		add(new CssSwitchingLink("var4", "tabpanel4"));
	}

	protected class CssSwitchingLink extends Link
	{
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
		 * @see wicket.markup.html.link.Link#onClick()
		 */
		public void onClick()
		{
			TabbedPanelPage.this.setModelObject(clazz);
		}

		/**
		 * @see wicket.markup.html.link.Link#isEnabled()
		 */
		public boolean isEnabled()
		{
			return !TabbedPanelPage.this.getModelObjectAsString().equals(clazz);
		}

	};

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


	protected void explain()
	{
		String html = "<span wicket:id=\"tabs\" class=\"tabpanel\">[tabbed panel will be here]</span>\n";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;List tabs=new ArrayList();<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;tabs.add(new AbstractTab(new Model(\"first tab\")) {<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;public Panel getPanel(String panelId) { return new TabPanel1(panelId); }<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;});<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;tabs.add(new AbstractTab(new Model(\"second tab\")) {<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;public Panel getPanel(String panelId) { return new TabPanel2(panelId); }<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;});<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;tabs.add(new AbstractTab(new Model(\"third tab\")) {<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;public Panel getPanel(String panelId) { return new TabPanel3(panelId); }<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;});<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;add(new TabbedPanel(\"tabs\", tabs)<br/>";
		add(new ExplainPanel(html, code));
	}
}
