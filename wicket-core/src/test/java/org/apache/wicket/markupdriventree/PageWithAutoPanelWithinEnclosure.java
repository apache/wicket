package org.apache.wicket.markupdriventree;

import org.apache.wicket.Auto;
import org.apache.wicket.markupdriventree.components.PanelA;

/**
 *
 */
public class PageWithAutoPanelWithinEnclosure extends BasePage
{
	@Auto
	PanelA panelA;

	public PageWithAutoPanelWithinEnclosure()
	{
		panelA = new PanelA("panelA");
	}
}
