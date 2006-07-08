/*
 * $Id: AutoComponentResolver.java 5871 2006-05-25 22:41:52 +0000 (Thu, 25 May
 * 2006) eelco12 $ $Revision$ $Date: 2006-05-25 22:41:52 +0000 (Thu, 25
 * May 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.resolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.util.lang.Classes;

/**
 * &lt;wicket:component class="myApp.MyTable" key=value&gt; tags may be used to
 * add Wicket components (e.g. a specialized PageableListView) and pass
 * parameters (e.g. the number of rows per list view page). The object is
 * automatically instantiated, initialized and added to the page's component
 * hierarchy.
 * <p>
 * Note: The component must have a constructor with a single String parameter:
 * the component name.
 * <p>
 * Note: The component must provide a setter for each key/value attribute
 * provided.
 * 
 * @author Juergen Donnerstag
 */
public final class AutoComponentResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	static
	{
		// register "wicket:fragement"
		WicketTagIdentifier.registerWellKnownTagName("component");
	}

	/**
	 * Temporary storage for containers currently being rendered. Thus child
	 * components can be re-parented. Remember: <wicket:component> are an
	 * exception to the rule. Though the markup of the children are nested
	 * inside <wicket:component>, their respective Java components are not. They
	 * must be added to the parent container of <wicket:component>.
	 */
	private final Map<Component, Object> nestedComponents = new HashMap<Component, Object>();

	/**
	 * @see wicket.markup.resolver.IComponentResolver#resolve(MarkupContainer,
	 *      MarkupStream, ComponentTag)
	 * @param container
	 *            The container parsing its markup
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @return true, if componentId was handle by the resolver. False, otherwise
	 */
	public final boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		// It must be <wicket:...>
		if (tag.isWicketTag())
		{
			// It must be <wicket:component...>
			if (tag.isComponentTag())
			{
				// Create and initialize the component
				final Component component = createComponent(container, tag);
				if (component != null)
				{
					// 1. push the current component onto the stack
					nestedComponents.put(component, null);

					try
					{
						// 2. Add it to the hierarchy and render it
						component.autoAdded();
					}
					finally
					{
						// 3. remove it from the stack
						nestedComponents.remove(component);
					}

					return true;
				}
			}
		}

		// Re-parent children of <wicket:component>.
		if ((tag.getId() != null) && nestedComponents.containsKey(container))
		{
			MarkupContainer parent = container.getParent();

			// Take care of nested <wicket:component>
			while ((parent != null) && nestedComponents.containsKey(parent))
			{
				parent = parent.getParent();
			}

			if (parent != null)
			{
				final Component component = parent.get(tag.getId());
				if (component != null)
				{
					component.render(markupStream);
					return true;
				}
			}
		}

		// We were not able to handle the componentId
		return false;
	}

	/**
	 * Based on the tag, create and initalize the component.
	 * 
	 * @param container
	 *            The current container. The new compent will be added to that
	 *            container.
	 * @param tag
	 *            The tag containing the information about component
	 * @return The new component
	 * @throws WicketRuntimeException
	 *             in case the component could not be created
	 */
	// Wicket is current not using any bean util jar, which is why ...
	private final Component createComponent(final MarkupContainer container, final ComponentTag tag)
	{
		// If no component name is given, create a page-unique one yourself.
		String componentId = tag.getAttributes().getString("name");
		if (componentId == null)
		{
			componentId = Component.AUTO_COMPONENT_PREFIX + container.getPage().getAutoIndex();
		}
		else
		{
			// TODO can we just alter the name???? We have to prefix it.
			componentId = Component.AUTO_COMPONENT_PREFIX + componentId;
			// Can i set it??
			// tag.setId(componentId);
		}

		// Get the component class name
		final String classname = tag.getAttributes().getString("class");
		if ((classname == null) || (classname.trim().length() == 0))
		{
			throw new MarkupException("Tag <wicket:component> must have attribute 'class'");
		}

		// Load the class. In case a Groovy Class Resolver has been provided,
		// the name might be a Groovy file.
		// Note: Spring based components are not supported this way. May be we
		// should provide a ComponentFactory like we provide a PageFactory.
		final Class componentClass = container.getSession().getClassResolver().resolveClass(
				classname);

		// construct the component. It must have a constructor with a single
		// String (componentId) parameter.
		final Component component;
		try
		{
			final Constructor constructor = componentClass.getConstructor(new Class[] {
					MarkupContainer.class, String.class });
			component = (Component)constructor.newInstance(new Object[] { container, componentId });
		}
		catch (NoSuchMethodException e)
		{
			throw new MarkupException("Unable to create Component from wicket tag: Cause: "
					+ e.getMessage());
		}
		catch (InvocationTargetException e)
		{
			throw new MarkupException("Unable to create Component from wicket tag: Cause: "
					+ e.getMessage());
		}
		catch (IllegalAccessException e)
		{
			throw new MarkupException("Unable to create Component from wicket tag: Cause: "
					+ e.getMessage());
		}
		catch (InstantiationException e)
		{
			throw new MarkupException("Unable to create Component from wicket tag: Cause: "
					+ e.getMessage());
		}
		catch (ClassCastException e)
		{
			throw new MarkupException("Unable to create Component from wicket tag: Cause: "
					+ e.getMessage());
		}
		catch (SecurityException e)
		{
			throw new MarkupException("Unable to create Component from wicket tag: Cause: "
					+ e.getMessage());
		}

		// Get all remaining attributes and invoke the component's setters
		Iterator iter = tag.getAttributes().entrySet().iterator();
		while (iter.hasNext())
		{
			final Map.Entry entry = (Map.Entry)iter.next();
			final String key = (String)entry.getKey();
			final String value = (String)entry.getValue();

			// Ignore attributes 'name' and 'class'
			if ("name".equalsIgnoreCase(key) || ("class".equalsIgnoreCase(key)))
			{
				continue;
			}

			Classes.invokeSetter(component, key, value, container.getLocale());
		}

		return component;
	}
}