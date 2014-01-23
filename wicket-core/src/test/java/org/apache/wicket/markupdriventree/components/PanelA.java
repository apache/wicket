package org.apache.wicket.markupdriventree.components;

import org.apache.wicket.Auto;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 */
public class PanelA extends Panel
{
	@Auto
	ComponentA a;

	public PanelA(String id)
	{
		super(id);

		a = new ComponentA("a");
	}
}
