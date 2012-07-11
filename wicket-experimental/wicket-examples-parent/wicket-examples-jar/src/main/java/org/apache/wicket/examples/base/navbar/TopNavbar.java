package org.apache.wicket.examples.base.navbar;

import org.apache.wicket.examples.base.markup.ClassValue;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.Panel;

public class TopNavbar extends Panel {
	private static final long serialVersionUID = 1L;

	public TopNavbar(String id) {
		super(id);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		ClassValue css = ClassValue.of(tag.getAttribute("class"));
		css.with("navbar").with("navbar-fixed-top");
		tag.put("class", css.toString());
	}
}
