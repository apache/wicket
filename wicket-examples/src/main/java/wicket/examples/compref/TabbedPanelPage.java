package wicket.examples.compref;

import java.util.ArrayList;
import java.util.List;

import wicket.AttributeModifier;
import wicket.MarkupContainer;
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
public class TabbedPanelPage extends WicketExamplePage<String>
{
	/**
	 * Constructor
	 */
	public TabbedPanelPage()
	{
		setModel(new Model<String>("tabpanel"));

		// create links used to switch between css variations
		addCssSwitchingLinks();

		// create a list of ITab objects used to feed the tabbed panel
		List<AbstractTab> tabs = new ArrayList<AbstractTab>();
		tabs.add(new AbstractTab(new Model<String>("first tab"))
		{
			@Override
			public Panel getPanel(final MarkupContainer parent, final String panelId)
			{
				return new TabPanel1(parent, panelId);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("second tab"))
		{
			@Override
			public Panel getPanel(final MarkupContainer parent, final String panelId)
			{
				return new TabPanel2(parent, panelId);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("third tab"))
		{
			@Override
			public Panel getPanel(final MarkupContainer parent, final String panelId)
			{
				return new TabPanel3(parent, panelId);
			}
		});

		// add the new tabbed panel, attribute modifier only used to switch
		// between different css variations
		new TabbedPanel(this, "tabs", tabs).add(new AttributeModifier("class", true,
				TabbedPanelPage.this.getModel()));

	}

	private void addCssSwitchingLinks()
	{
		new CssSwitchingLink(this, "var0", "tabpanel");
		new CssSwitchingLink(this, "var1", "tabpanel1");
		new CssSwitchingLink(this, "var2", "tabpanel2");
		new CssSwitchingLink(this, "var3", "tabpanel3");
		new CssSwitchingLink(this, "var4", "tabpanel4");
	}

	protected class CssSwitchingLink extends Link
	{
		private final String clazz;

		/**
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 * @param clazz
		 */
		public CssSwitchingLink(final MarkupContainer parent, final String id, final String clazz)
		{
			super(parent, id);
			this.clazz = clazz;
		}

		/**
		 * @see wicket.markup.html.link.Link#onClick()
		 */
		@Override
		public void onClick()
		{
			TabbedPanelPage.this.setModelObject(clazz);
		}

		/**
		 * @see wicket.markup.html.link.Link#isEnabled()
		 */
		@Override
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
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 *            component id
		 */
		public TabPanel1(final MarkupContainer parent, final String id)
		{
			super(parent, id);
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
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 *            component id
		 */
		public TabPanel2(final MarkupContainer parent, final String id)
		{
			super(parent, id);
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
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 *            component id
		 */
		public TabPanel3(final MarkupContainer parent, final String id)
		{
			super(parent, id);
		}

	};


	@Override
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
		new ExplainPanel(this, html, code);
	}
}
