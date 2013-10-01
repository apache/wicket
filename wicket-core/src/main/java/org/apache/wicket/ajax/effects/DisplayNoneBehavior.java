package org.apache.wicket.ajax.effects;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;

/**
 * A behavior that hides a component by using CSS <em>display</em> rule
 */
class DisplayNoneBehavior extends AttributeAppender
{
	DisplayNoneBehavior()
	{
		super("style", Model.of("display: none"));
	}

	@Override
	public boolean isTemporary(Component component)
	{
		return true;
	}
}
