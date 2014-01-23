package org.apache.wicket.markupdriventree;

import org.apache.wicket.Auto;
import org.apache.wicket.markupdriventree.components.PanelA;

/**
 *
 */
public class PageWithManuallyAddedPanel extends BasePageWithPanel
{
	public PageWithManuallyAddedPanel()
	{
		PanelA panelA = new PanelA("panelA");
		add(panelA);
	}
}
