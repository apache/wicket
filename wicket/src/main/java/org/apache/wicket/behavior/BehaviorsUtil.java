/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		List<? extends IBehavior> behaviors = component.getBehaviors();
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
