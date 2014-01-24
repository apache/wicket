package org.apache.wicket;

import org.apache.wicket.application.IComponentInitializationListener;

/**
 *
 */
public class MarkupDrivenTreeInitializionListener implements IComponentInitializationListener
{
	@Override
	public void onInitialize(Component component)
	{
		if (component instanceof MarkupContainer)
		{
			ComponentTreeBuilder builder = new ComponentTreeBuilder();
			builder.rebuild((MarkupContainer) component);
		}
	}
}
