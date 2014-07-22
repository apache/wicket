package org.apache.wicket.markup.html.form;

import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentInstantiationListener;

/**
 * Restores the rendering behavior of RadioChoice until Wicket 6.x.
 * That means:
 * <ul>
 *     <li>Uses <xmp><br/></xmp> as a suffix</li>
 *     <li>renders the label after the checkbox</li>
 * </ul>
 *
 * @deprecated
 */
@Deprecated
public class RadioChoiceWicket6Listener implements IComponentInstantiationListener
{
	@Override
	public void onInstantiation(Component component)
	{
		if (component instanceof RadioChoice<?>)
		{
			RadioChoice<?> radioChoice = (RadioChoice<?>) component;
			radioChoice.setSuffix("<br />\n");
			radioChoice.setLabelPosition(AbstractChoice.LabelPosition.AFTER);
		}
	}
}
