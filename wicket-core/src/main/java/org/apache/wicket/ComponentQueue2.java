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
package org.apache.wicket;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.util.lang.Args;

/**
 * Manages the component queue.
 * 
 * @author igor
 */
class ComponentQueue2
{
	private static final int INITIAL = 8;

	private Map<String, Component> queue;
	private int queueSize = 0;

	void add(Component... components)
	{
		for (Component component : components)
		{
			add(component);
		}
	}

	void add(Component component)
	{
		Args.notNull(component, "component");

		if (queue == null)
		{
			queue = new HashMap<>(INITIAL, 1);
		}

		Component old = queue.put(component.getId(), component);
		if (old != null)
		{
			throw new WicketRuntimeException("A component with id: " + component.getId()
					+ " has already been queued");
		}
		queueSize++;
	}

	Component remove(String id)
	{
		if (queue != null)
		{
			Component removed = queue.remove(id);
			if (removed != null)
			{
				queueSize--;
				if (isEmpty())
				{
					queue = null;
				}
				return removed;
			}
		}
		return null;
	}

	public boolean isEmpty()
	{
		return queueSize == 0;
	}

	public Component get(String id)
	{
		if (queue != null)
		{
			return queue.get(id);
		}
		return null;
	}

	@Override
	public String toString()
	{
		return "ComponentQueue{" +
				"queueSize=" + queueSize +
				", queue=" + queue +
				'}';
	}
}
