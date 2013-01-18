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

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;

/**
 * Test page used by the {@link RemoveTest}
 */
public class RemoveTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	static final String COMPONENT = "component";
	static final String LINK = "link";

	MarkupContainer _1;
	MarkupContainer _2;

	private int componentOnRemovalFromHierarchyCalls = 0;
	private int linkOnRemovalFromHierarchyCalls = 0;
	private int behaviorOnRemovalCalls = 0;

	/**
	 * Construct.
	 */
	public RemoveTestPage()
	{
		_1 = new MarkupContainer(COMPONENT)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onRemove()
			{
				componentOnRemovalFromHierarchyCalls++;
				super.onRemove();
			}
		};
		_1.add(new Behavior()
		{
			@Override
			public void onRemove(Component component)
			{
				behaviorOnRemovalCalls++;
				super.onRemove(component);
			}
		});
		_1.add(new Link<Void>(LINK)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onRemove()
			{
				linkOnRemovalFromHierarchyCalls++;
				super.onRemove();
			}

			@Override
			public void onClick()
			{
				_1.replaceWith(_2);
			}
		});

		_2 = new MarkupContainer(COMPONENT)
		{
			private static final long serialVersionUID = 1L;
		};
		_2.add(new Link<Void>(LINK)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onRemove()
			{
				linkOnRemovalFromHierarchyCalls++;
				// disable the super call on purpose
				// to provoke an exception.
				// super.onRemovalFromHierarchy();
			}

			@Override
			public void onClick()
			{
				_2.replaceWith(_1);
			}
		});

		add(_1);
	}

	/**
	 * @return componentOnRemovalFromHierarchyCalls
	 */
	public int getComponentOnRemovalFromHierarchyCalls()
	{
		return componentOnRemovalFromHierarchyCalls;
	}

	/**
	 * @return linkOnRemovalFromHierarchyCalls
	 */
	public int getLinkOnRemovalFromHierarchyCalls()
	{
		return linkOnRemovalFromHierarchyCalls;
	}


	/**
	 * @return behaviorOnRemovalCalls
	 */
	public int getBehaviorOnRemovalCalls()
	{
		return behaviorOnRemovalCalls;
	}
}
