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
package org.apache.wicket.ng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.ng.request.component.RequestableComponent;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.util.string.Strings;

/**
 * Naive component implementation. Should be enough for some basic tests
 * 
 * @author Matej Knopp
 */
public class Component2 implements RequestableComponent
{
	private static final long serialVersionUID = 1L;

	private final String id;

	public Component2(String id)
	{
		this.id = id;
	}

	private final List<Component2> children = new ArrayList<Component2>();

	public List<Component2> getChildren()
	{
		return Collections.unmodifiableList(children);
	}

	private Component2 parent;

	public Component2 getParent()
	{
		return parent;
	}

	public void remove(Component2 Component2)
	{
		if (Component2 == null)
		{
			throw new NullPointerException("Argument 'Component2' may not be null.");
		}
		if (Component2.getParent() != this)
		{
			throw new IllegalStateException("Component2 is not child of this Component2.");
		}
		children.remove(Component2);
		Component2.parent = null;
	}

	public void add(Component2 Component2)
	{
		if (Component2 == null)
		{
			throw new NullPointerException("Argument 'Component2' may not be null.");
		}
		if (Component2.getParent() != null)
		{
			throw new IllegalStateException("Component2 is already added to another Component2.");
		}
		for (Component2 c : children)
		{
			if (c.getId().equals(Component2.getId()))
			{
				throw new IllegalStateException(
					"Component2 with same id already added to this Component2.");
			}
		}
		Component2.parent = this;
		children.add(Component2);
	}

	public boolean isEnabled()
	{
		return true;
	}

	public boolean isVisible()
	{
		return true;
	}

	public boolean canCallListenerInterface()
	{
		if (!isEnabled() || !isVisible())
		{
			return false;
		}
		else if (getParent() != null)
		{
			return getParent().canCallListenerInterface();
		}
		else
		{
			return false;
		}
	}

	private static final char PATH_SEPARATOR = ':';

	public Component2 get(String path)
	{
		String first = Strings.firstPathComponent(path, PATH_SEPARATOR);
		String after = Strings.afterFirstPathComponent(path, PATH_SEPARATOR);
		for (Component2 c : getChildren())
		{
			if (first.equals(c.getId()))
			{
				if (Strings.isEmpty(after))
				{
					return c;
				}
				else
				{
					return c.get(after);
				}
			}
		}
		return null;
	}

	private final List<IBehavior> behaviors = new ArrayList<IBehavior>();

	public void add(IBehavior behavior)
	{
		behaviors.add(behavior);
	}

	public List<IBehavior> getBehaviors()
	{
		return Collections.unmodifiableList(behaviors);
	}

	public String getId()
	{
		return id;
	}

	private String markupId;

	public String getMarkupId(boolean createIfDoesNotExist)
	{
		if (markupId == null && createIfDoesNotExist)
		{
			throw new RuntimeException("check code");
			// markupId = getId() + getPage().getMarkupIdConterNextValue();
		}
		return markupId;
	}

	public Page2 getPage()
	{
		if (getParent() != null)
		{
			return getParent().getPage();
		}
		else
		{
			return null;
		}
	}

	public String getPath()
	{
		if (getParent() instanceof RequestablePage)
		{
			return getId();
		}
		else
		{
			return getId() + PATH_SEPARATOR + getParent().getPath();
		}
	}

	protected void onDetach()
	{

	}

	public void detach()
	{
		for (Component2 c : getChildren())
		{
			c.detach();
		}
		onDetach();
	}

	public final void prepareForRender(boolean setRenderingFlag)
	{
		for (Component2 c : getChildren())
		{
			c.prepareForRender(setRenderingFlag);
		}
	}

	public void renderComponent()
	{
		for (Component2 c : children)
		{
			c.renderComponent();
		}
	};
}
