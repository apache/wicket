package org.apache.wicket.markupdriventree;

import org.apache.wicket.Auto;
import org.apache.wicket.markupdriventree.components.PanelA;

/**
 *
 */
public class PageWithAutoPanel extends BasePageWithPanel
{
	@Auto
	PanelA panelA;

	public PageWithAutoPanel()
	{
		panelA = new PanelA("panelA");
	}
}
