package org.apache.wicket.ng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ng.behavior.IBehavior;
import org.apache.wicket.ng.request.component.RequestableComponent;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.util.string.Strings;

/**
 * Naive component implementation. Should be enough for some basic tests
 * 
 * @author Matej Knopp
 */
public class Component implements RequestableComponent
{
	private static final long serialVersionUID = 1L;

	private final String id;

	public Component(String id)
	{
		this.id = id;
	}

	private final List<Component> children = new ArrayList<Component>();

	public List<Component> getChildren()
	{
		return Collections.unmodifiableList(children);
	}

	private Component parent;

	public Component getParent()
	{
		return parent;
	}

	public void remove(Component component)
	{
		if (component == null)
		{
			throw new NullPointerException("Argument 'component' may not be null.");
		}
		if (component.getParent() != this)
		{
			throw new IllegalStateException("Component is not child of this component.");
		}
		children.remove(component);
		component.parent = null;
	}

	public void add(Component component)
	{
		if (component == null)
		{
			throw new NullPointerException("Argument 'component' may not be null.");
		}
		if (component.getParent() != null)
		{
			throw new IllegalStateException("Component is already added to another component.");
		}
		for (Component c : children)
		{
			if (c.getId().equals(component.getId()))
			{
				throw new IllegalStateException(
					"Component with same id already added to this component.");
			}
		}
		component.parent = this;
		children.add(component);
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

	public Component get(String path)
	{
		String first = Strings.firstPathComponent(path, PATH_SEPARATOR);
		String after = Strings.afterFirstPathComponent(path, PATH_SEPARATOR);
		for (Component c : getChildren())
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
			markupId = getId() + getPage().getMarkupIdConterNextValue();
		}
		return markupId;
	}

	public Page getPage()
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
		for (Component c : getChildren())
		{
			c.detach();
		}
		onDetach();
	}

	public final void prepareForRender(boolean setRenderingFlag)
	{
		for (Component c : getChildren())
		{
			c.prepareForRender(setRenderingFlag);
		}
	}

	public void renderComponent()
	{
		for (Component c : children)
		{
			c.renderComponent();
		}
	};
}
