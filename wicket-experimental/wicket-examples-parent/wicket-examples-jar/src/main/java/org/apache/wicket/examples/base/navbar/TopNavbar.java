package org.apache.wicket.examples.base.navbar;

import org.apache.wicket.examples.base.markup.ClassValue;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class TopNavbar extends Panel
{
	private static final long serialVersionUID = 1L;
	private RepeatingView menuitems;

	public TopNavbar(String id)
	{
		super(id);
		menuitems = new RepeatingView("items");
		add(menuitems);
	}

	public void addMenuItem(String label, Class<? extends WebPage> page)
	{
		addMenuItem(label, page, null);
	}

	public void addMenuItem(String label, Class<? extends WebPage> page, PageParameters parameters)
	{
		menuitems.add(new TopNavbarMenuItem(menuitems.newChildId(), label, page, parameters));
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		ClassValue css = ClassValue.of(tag.getAttribute("class"));
		css.with("navbar").with("navbar-fixed-top");
		tag.put("class", css.toString());
	}
}
