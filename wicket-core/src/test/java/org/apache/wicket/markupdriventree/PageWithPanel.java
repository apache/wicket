package org.apache.wicket.markupdriventree;

import org.apache.wicket.Auto;
import org.apache.wicket.markupdriventree.components.PanelA;

/**
 *
 */
public class PageWithPanel extends BasePage
{
	@Auto
	PanelA panelA;

	public PageWithPanel()
	{
		panelA = new PanelA("panelA");
	}
}
