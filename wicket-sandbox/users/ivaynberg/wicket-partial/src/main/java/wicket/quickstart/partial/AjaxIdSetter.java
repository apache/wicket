package wicket.quickstart.partial;

import wicket.Component;
import wicket.behavior.AbstractBehavior;
import wicket.behavior.IBehavior;
import wicket.markup.ComponentTag;

public class AjaxIdSetter extends AbstractBehavior
{
	public static final IBehavior INSTANCE = new AjaxIdSetter();

	private AjaxIdSetter()
	{

	}

	public void onComponentTag(Component component, ComponentTag tag)
	{
		super.onComponentTag(component, tag);
		tag.put("id", component.getPageRelativePath());
	}
}
