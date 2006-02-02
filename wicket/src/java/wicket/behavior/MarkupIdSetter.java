package wicket.behavior;

import wicket.Component;
import wicket.markup.ComponentTag;

/**
 * Attirbute modifier that adds component's markup id if none was set.
 * 
 * @see wicket.Component#getMarkupId()
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public final class MarkupIdSetter extends AbstractBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * Singleton instance
	 */
	public static final IBehavior INSTANCE = new MarkupIdSetter();

	private MarkupIdSetter()
	{
		// force singleton
	}

	/**
	 * @see wicket.behavior.AbstractBehavior#onComponentTag(wicket.Component,
	 *      wicket.markup.ComponentTag)
	 */
	public void onComponentTag(Component component, ComponentTag tag)
	{
		super.onComponentTag(component, tag);
		if (!tag.getAttributes().containsKey("id"))
		{
			tag.put("id", component.getMarkupId());
		}
	}

}
