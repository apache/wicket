package wicket.examples.tabPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wicket.Component;
import wicket.AttributeModifier;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.PropertyModel;

/**
 * Simple Tab panel like in swing. For this to work all components must be named "tab" and
 * should be of the same type (e.g. all Panels) (although Panels and Labels both work with
 * a div (the default html element)). If you use other components as Panels you may have
 * to modify TabPanel.html and use another html element.
 * @author Marrink
 */
public class TabPanel extends Panel
{
	// Just so people won't think i like finals, The original code did not contain them ;)
	final private List tabs;

	private int selectedTab;

	/**
	 * Construct.
	 * @param componentName
	 */
	public TabPanel(final String componentName)
	{
		super(componentName);

		selectedTab = -1;
		tabs = new ArrayList();

		add(new ListView("tabbar", tabs)
		{
			protected void populateItem(final ListItem listItem)
			{
				final TabPanelModel model = (TabPanelModel) listItem.getModelObject();
				final int index = listItem.getIndex();
				final Link tab = new Link("tablink", model)
				{
					public void linkClicked()
					{
						selectTab(((TabPanelModel) getModel()).getIndex());
					}
				};

				tab.setAutoEnable(false);
				tab.add(new Label("tablabel", model, "label"));

				tab.add(new AttributeModifier("disabled", true, new PropertyModel(
						model, "disabled")));

				tab.add(new AttributeModifier("class", true, new PropertyModel(model,
						"getHTMLClass()")));

				listItem.add(tab);
			}
		});

		add(new Label("tab", "xxx")); // dummy placeholder else replace will fail
	}

	/**
	 * Adds a new tab to this Panel. It is not selected but is enabled. Just because you
	 * might forget to select a tab, the first enabled tab it encounters is automaticly
	 * selected.
	 * @param label label for the tab
	 * @param panel component to display when the tab is selected
	 */
	public void addTab(final String label, final Component panel)
	{
		addTab(new TabModel(this, label, panel));
	}

	/**
	 * @param label
	 * @param selected
	 * @param panel
	 */
	public void addTab(final String label, final boolean selected, final Component panel)
	{
		addTab(new TabModel(this, label, panel, selected, true));
	}

	/**
	 * @param label
	 * @param selected
	 * @param enabled
	 * @param panel
	 */
	public void addTab(final String label, final boolean selected, final boolean enabled,
			final Component panel)
	{
		addTab(new TabModel(this, label, panel, selected, enabled));
	}

	/**
	 * Adds a new tab to the panel
	 * @param tab the model to provide all the information and maintain the state of this
	 *            tab
	 */
	public void addTab(final TabPanelModel tab)
	{
		tabs.add(tab);

		if (tab.isSelected())
		{
			if (tab.isEnabled())
			{
				selectTab(indexOf(tab));
			}
			else
			{
				tab.setSelected(false);
			}
		}

		if ((selectedTab < 0) && tab.isEnabled())
		{
			selectTab(indexOf(tab));
		}
	}

	// TODO indexOf by label and component, for now just use the list of tabs
	/**
	 * The index of a certain tab
	 * @param model
	 * @return -1 if the tab is not on this panel, or a number specifying the index of the
	 *         tab
	 */
	public int indexOf(final TabPanelModel model)
	{
		return tabs.indexOf(model);
	}

	/**
	 * internal method that will return null if no tab is selected
	 * @return null if no tab is selected or the selected tab
	 */
	private TabPanelModel getSelectedTabModel()
	{
		if (selectedTab < 0)
		{
			return null;
		}
		else
		{
			return (TabPanelModel) tabs.get(selectedTab);
		}
	}

	/**
	 * Returns the tab that is selected, if no tab is selected it will select and return
	 * the first enabled tab. if there are no enabled tabs it will return null.
	 * @return the selected tab.
	 */
	public TabPanelModel getSelectedTab()
	{
		if (selectedTab < 0)
		{
			for (int i = 0; i < tabs.size(); i++)
			{
				final TabPanelModel model = (TabPanelModel) tabs.get(i);
				if (model.isEnabled())
				{
					selectTab(i);
					break;
				}
			}

			if (selectedTab < 0)
			{
				return null;
			}
		}

		return (TabPanelModel) tabs.get(selectedTab);
	}

	/**
	 * Selects a tab.
	 * @param index of the tab
	 */
	public void selectTab(final int index)
	{
		final TabPanelModel model = (TabPanelModel) tabs.get(index);
		if (!model.isEnabled())
		{
			throw new RuntimeException("Tab is disabled.");
		}

		if (model.isSelected())
		{
			selectedTab = index;
			return;
		}

		final TabPanelModel old = getSelectedTabModel();
		if (old != null)
		{
			old.setSelected(false);
		}

		model.setSelected(true);
		selectedTab = index;
		replace(model.getComponent());
	}

	/**
	 * Disables a tab.
	 * @param index tab index
	 * @throws RuntimeException if the tab is selected.
	 */
	public void disableTab(final int index)
	{
		final TabPanelModel model = (TabPanelModel) tabs.get(index);
		if (model.isSelected())
		{
			throw new RuntimeException("Kan geselecteerde tab niet disabelen.");
		}

		model.setEnabled(false);
	}

	/**
	 * Enables a tab.
	 * @param index tab index
	 */
	public void enableTab(final int index)
	{
		((TabPanelModel) tabs.get(index)).setEnabled(true);
	}

	/**
	 * Toggles a tab between enabled and disabled.
	 * @param index tab index
	 * @throws RuntimeException if the tab is selected.
	 */
	public void toggleTab(final int index)
	{
		final TabPanelModel model = (TabPanelModel) tabs.get(index);
		if (model.isSelected())
		{
			throw new RuntimeException("Kan geselecteerde tab niet disabelen.");
		}

		// if selected enabled must be true
		model.setEnabled(!model.isEnabled());
	}

	/**
	 * @return the number of tabs on this panel.
	 */
	public int tabCount()
	{
		return tabs.size();
	}

	/**
	 * @param index tab index
	 * @return tab at specified index
	 */
	public TabPanelModel getTab(final int index)
	{
		return (TabPanelModel) tabs.get(index);
	}

	/**
	 * All the tabs on this panel
	 * @return an unmodifiable list containing all the tabs on this panel.
	 */
	public List getTabs()
	{
		return Collections.unmodifiableList(tabs);
	}

	/**
	 * Default implementation of a TabPanelModel.
	 * @author Marrink
	 */
	private class TabModel implements TabPanelModel
	{
		private Component panel;

		final private TabPanel parent;

		private String label;

		private boolean selected;

		private boolean enabled;

		/**
		 * Constructs a new enabled TabModel with the specified label. the Tab is not
		 * selected.
		 * @param parent which holds the tab
		 * @param label for the tab
		 * @param panel component to display if this tab is selected
		 */
		public TabModel(final TabPanel parent, final String label, final Component panel)
		{
			this(parent, label, panel, false, true);
		}

		/**
		 * Construct.
		 * @param parent which holds the tab
		 * @param label for the tab
		 * @param panel component to display if this tab is selected
		 * @param selected state of the tab
		 * @param enabled state of the tab
		 */
		public TabModel(final TabPanel parent, final String label, final Component panel,
				final boolean selected, final boolean enabled)
		{
			this.parent = parent;
			this.label = label;
			this.selected = selected;
			this.enabled = enabled;
			this.panel = panel;
		}

		/**
		 * Returns this model. Usefull for using models with expressions.
		 * @see wicket.model.IModel#getObject()
		 */
		public Object getObject()
		{
			return this;
		}

		/**
		 * Disabled. You cant change the object of this model.
		 * @see wicket.model.IModel#setObject(java.lang.Object)
		 */
		public void setObject(final Object object)
		{
			// disabled
		}

		/**
		 * @param selected
		 * @see TabPanelModel#setSelected(boolean)
		 */
		public void setSelected(final boolean selected)
		{
			this.selected = selected;
		}

		/**
		 * @return boolean
		 * @see TabPanelModel#isSelected()
		 */
		public boolean isSelected()
		{
			return selected;
		}

		/**
		 * @param enable
		 * @see TabPanelModel#setEnabled(boolean)
		 */
		public void setEnabled(final boolean enable)
		{
			this.enabled = enable;
		}

		/**
		 * @return boolean
		 * @see TabPanelModel#isEnabled()
		 */
		public boolean isEnabled()
		{
			return enabled;
		}

		/**
		 * This is used to put the disabled tag in place. by default a tag is not added if
		 * its value is null.
		 * @return null if enabled, true if disabled
		 * @see TabPanelModel#isDisabled()
		 */
		public Boolean isDisabled()
		{
			return (enabled ? null : Boolean.TRUE);
		}

		/**
		 * @return String
		 * @see TabPanelModel#getLabel()
		 */
		public String getLabel()
		{
			return label;
		}

		/**
		 * @param label
		 * @see TabPanelModel#setLabel(String)
		 */
		public void setLabel(final String label)
		{
			this.label = label;

		}

		/**
		 * @return Component
		 * @see TabPanelModel#getComponent()
		 */
		public Component getComponent()
		{
			return panel;
		}

		/**
		 * @param panel
		 * @see TabPanelModel#setComponent(Component)
		 */
		public void setComponent(final Component panel)
		{
			this.panel = panel;

		}

		/**
		 * @return int
		 * @see TabPanelModel#getIndex()
		 */
		public int getIndex()
		{
			return parent.indexOf(this);

		}

		/**
		 * @return String
		 * @see TabPanelModel#getHTMLClass()
		 */
		public String getHTMLClass()
		{
			return (isSelected() ? "selected" : "not-selected");
		}
	}

	/**
	 * @see wicket.Component#render()
	 */
	public void render()
	{
		setVisible(!tabs.isEmpty());
		super.render();
	}
}
