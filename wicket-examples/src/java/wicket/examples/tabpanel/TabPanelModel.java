/*
 * Created on Jan 10, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package wicket.examples.tabPanel;

import wicket.Component;
import wicket.model.IModel;


/**
 * @author Marrink
 */
public interface TabPanelModel extends IModel
{
	/**
	 * This method is used as flag by the TabPanel, do not modify it yourself in any
	 * otherway then through the TabPanel. if no tab at all is selected the first enabled
	 * tab is displayed.
	 * @param selected
	 */
	void setSelected(boolean selected); // only publicly accessable inside package

	/**
	 * @return true if this tab is showing its contents
	 */
	public boolean isSelected();

	/**
	 * Enables or disables this tab. If disabled the tab is displayed as greyed-out and
	 * cannot be selected / clicked on. please use TabPanel to modify this value
	 * @param enable
	 */
	void setEnabled(boolean enable); // only publicly accessable inside package

	/**
	 * @return true if this tab is enabled and thus clickable / selectable, false
	 *         otherwise
	 */
	public boolean isEnabled();

	/**
	 * This is used to put the disabled attribute on the tab. Since attributes are not
	 * added if they are null, this method should return null if the tab is enabled. and
	 * true if it is disabled.
	 * @return null if enabled, true if disabled.
	 */
	public Boolean isDisabled();

	/**
	 * The label of the tab.
	 * @return the label of the tab.
	 */
	public String getLabel();

	/**
	 * Changes the label of the tab.
	 * @param label the label of the tab
	 */
	public void setLabel(String label);

	/**
	 * The component to display when the tab is selected.
	 * @return the component.
	 */
	public Component getComponent();

	/**
	 * Changes the component that is displayed when the tab is selected.
	 * @param panel
	 */
	public void setComponent(Component panel);

	/**
	 * Returns the current index of this tab at the tabPanel
	 * @return the index of the tab
	 */
	public int getIndex();

	/**
	 * The HTML css class to use when rendering this tab, based on isSelected
	 * @return css class name.
	 */
	public String getHTMLClass();
}
