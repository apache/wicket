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
package org.apache.wicket.queueing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;

class Path implements Iterable<Path.Segment>
{

	private List<Segment> segments;

	public Path()
	{
		segments = new ArrayList<>();
	}

	public Path(Component... components)
	{
		this();
		add(components);
	}

	public Path add(Class<?> type, String id)
	{
		segments.add(new Segment(type, id));
		return this;
	}

	public Path add(String id)
	{
		add(Component.class, id);
		return this;
	}

	public Path add(Component... components)
	{
		for (Component c : components)
		{
			add(c.getClass(), c.getId());
		}
		return this;
	}


	@Override
	public Iterator<Path.Segment> iterator()
	{
		return segments.iterator();
	}

	public int size()
	{
		return segments.size();
	}

	public Segment get(int index)
	{
		return segments.get(index);
	}

	public static class Segment
	{
		Class<?> type;
		String id;

		public Segment(Class<?> type, String id)
		{
			this.type = type;
			this.id = id;
		}

		public Class<?> getType()
		{
			return type;
		}

		public String getId()
		{
			return id;
		}

	}
}
