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

import java.util.Arrays;

/**
 * Manages the component queue.
 * 
 * @author igor
 */
class ComponentQueue
{
	private static final int INITIAL = 8;
	private static final int ADDITIONAL = 8;

	private Component[] queue;
	private int queueSize = 0;
	private boolean dirty = false;
	private String[] seen;
	private int seenSize = 0;

	void add(Component... components)
	{
		// TODO queueing this can be a more efficient implementation since we know the size of the
		// array
		for (Component component : components)
		{
			add(component);
		}
	}

	void add(Component component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("component cannot be null");
		}

		if (seen != null)
		{
			for (int i = 0; i < seenSize; i++)
			{
				if (seen[i].equals(component.getId()))
				{
					throw new WicketRuntimeException("A component with id: " + component.getId()
						+ " has already been queued");
				}
			}
		}

		if (dirty)
		{
			if (queueSize == 0)
			{
				queue = null;
			}
			else
			{
				Component[] replacement = new Component[queueSize + ADDITIONAL];
				int pos = 0;
				for (int i = 0; i < queue.length; i++)
				{
					if (queue[i] != null)
					{
						replacement[pos++] = queue[i];
					}
				}
				queue = replacement;
			}
		}

		if (queue == null)
		{
			queue = new Component[INITIAL];
		}
		else if (queue.length == queueSize)
		{
			queue = Arrays.copyOf(queue, queue.length + ADDITIONAL);
		}
		queue[queueSize] = component;
		queueSize++;

		if (seen == null)
		{
			seen = new String[INITIAL];
		}
		else if (seenSize == seen.length)
		{
			seen = Arrays.copyOf(seen, seen.length + ADDITIONAL);
		}
		seen[seenSize] = component.getId();
		seenSize++;
	}

	Component remove(String id)
	{
		int seen = 0;
		for (int i = 0; i < queue.length && seen < queueSize; i++)
		{
			Component component = queue[i];
			if (component != null)
			{
				seen++;
				if (component.getId().equals(id))
				{
					queue[i] = null;
					dirty = true;
					queueSize--;
					return component;
				}
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
		for (Component component : queue)
		{
			if (component != null && component.getId().equals(id))
			{
				return component;
			}
		}
		return null;
	}
}
