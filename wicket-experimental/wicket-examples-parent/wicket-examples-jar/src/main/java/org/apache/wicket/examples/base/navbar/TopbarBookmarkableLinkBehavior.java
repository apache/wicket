package org.apache.wicket.examples.base.navbar;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.examples.base.markup.ClassValue;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class TopbarBookmarkableLinkBehavior extends Behavior {
	private static final long serialVersionUID = 1L;

	@Override
	public void bind(Component component) {
		assert component instanceof BookmarkablePageLink;

		super.bind(component);
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag) {
		super.onComponentTag(component, tag);

		BookmarkablePageLink<?> link = (BookmarkablePageLink<?>) component;
		ClassValue classValue = ClassValue.of(tag.getAttributes().getString(
				"class"));
		if (link.linksTo(link.getPage()))
			classValue.with("active");
		else
			classValue.without("active");

		tag.put("class", classValue.toString());
	}
}
