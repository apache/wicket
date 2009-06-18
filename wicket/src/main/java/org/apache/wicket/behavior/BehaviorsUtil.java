package org.apache.wicket.behavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;

/**
 * This class contains a convenience method neccesary only until 1.5 when we can make
 * {@link Component#getBehaviors(Class)} public
 * 
 * @author Antony Stubbs
 * @author igor.vaynberg
 * 
 * @since 1.4
 * 
 */
// FIXME 1.5: remove
public class BehaviorsUtil
{

	/**
	 * Returns all behaviors attached to a component which implement a given type. A workaround
	 * until WICKET-2115 is resolved.
	 * 
	 * @param <M>
	 *            The type of behavior
	 * @param component
	 *            target componet
	 * @param type
	 *            type of behaviors to look for, <code>null</code> for all
	 * @return unmodifiable list of behaviors
	 */
	public static <M extends IBehavior> List<IBehavior> getBehaviors(Component component,
		Class<M> type)
	{
		List<IBehavior> behaviors = component.getBehaviors();
		if (behaviors == null)
		{
			return Collections.emptyList();
		}

		List<IBehavior> subset = new ArrayList<IBehavior>(behaviors.size());
		for (IBehavior behavior : behaviors)
		{
			if (behavior != null)
			{
				if (type == null)
				{
					subset.add(behavior);
				}
				else if (type.isAssignableFrom(behavior.getClass()))
				{
					subset.add(type.cast(behavior));
				}
			}
		}
		return Collections.unmodifiableList(subset);
	}

}
