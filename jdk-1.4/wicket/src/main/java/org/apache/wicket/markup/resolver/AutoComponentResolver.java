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
package org.apache.wicket.markup.resolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.util.convert.IConverter;


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
		// register "wicket:component"
		WicketTagIdentifier.registerWellKnownTagName("component");
	}

	/**
	 * Temporary storage for containers currently being rendered. Thus child
	 * components can be re-parented. Remember: <wicket:component> are an
	 * exception to the rule. Though the markup of the children are nested
	 * inside <wicket:component>, their respective Java components are not. They
	 * must be added to the parent container of <wicket:component>.
	 */
	private final Map nestedComponents = new HashMap();

	/**
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(MarkupContainer,
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
		if (tag instanceof WicketTag)
		{
			// It must be <wicket:component...>
			final WicketTag wicketTag = (WicketTag)tag;
			if (wicketTag.isComponentTag())
			{
				// Create and initialize the component
				final Component component = createComponent(container, wicketTag);
				if (component != null)
				{
					// 1. push the current component onto the stack
					nestedComponents.put(component, null);

					try
					{
						// 2. Add it to the hierarchy and render it
						container.autoAdd(component);
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
	private final Component createComponent(final MarkupContainer container, final WicketTag tag)
	{
		// If no component name is given, create a page-unique one yourself.
		String componentId = tag.getNameAttribute();
		if (componentId == null)
		{
			componentId = "anonymous-" + container.getPage().getAutoIndex();
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
			final Constructor constructor = componentClass
					.getConstructor(new Class[] { String.class });
			component = (Component)constructor.newInstance(new Object[] { componentId });
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

			invokeSetter(component, key, value, container.getLocale());
		}

		return component;
	}

	/**
	 * Invoke the setter method for 'name' on object and provide the 'value'
	 * 
	 * @param object
	 * @param name
	 * @param value
	 * @param locale
	 */
	private final void invokeSetter(final Object object, final String name, final String value,
			final Locale locale)
	{
		// Note: tag attributes are maintained in a LowerCaseKeyValueMap, thus
		// 'name' will be all lowercase.

		// Note: because the attributes are all lowercase, there is slight
		// possibility of error due to naming issues.

		// Note: all setters must start with "set"

		// Get the setter for the attribute
		final String methodName = "set" + name;
		final Method[] methods = object.getClass().getMethods();
		Method method = null;
		for (int i = 0; i < methods.length; i++)
		{
			if (methods[i].getName().equalsIgnoreCase(methodName))
			{
				method = methods[i];
			}
		}

		if (method == null)
		{
			throw new MarkupException("Unable to initialize Component. Method with name "
					+ methodName + " not found");
		}

		// The method must have a single parameter
		final Class[] parameterClasses = method.getParameterTypes();
		if (parameterClasses.length != 1)
		{
			throw new MarkupException("Unable to initialize Component. Method with name "
					+ methodName + " must have one and only one parameter");
		}

		// Convert the parameter if necessary, depending on the setter's
		// attribute
		final Class paramClass = parameterClasses[0];
		try
		{
			final IConverter converter = Application.get().getConverterLocator().getConverter(
					paramClass);
			final Object param = converter.convertToObject(value, locale);
			if (param == null)
			{
				throw new MarkupException("Unable to convert value '" + value + "' into "
						+ paramClass + ". May be there is no converter for that type registered?");
			}
			method.invoke(object, new Object[] { param });
		}
		catch (IllegalAccessException ex)
		{
			throw new MarkupException(
					"Unable to initialize Component. Failure while invoking method " + methodName
							+ ". Cause: " + ex);
		}
		catch (InvocationTargetException ex)
		{
			throw new MarkupException(
					"Unable to initialize Component. Failure while invoking method " + methodName
							+ ". Cause: " + ex);
		}
		catch (NumberFormatException ex)
		{
			throw new MarkupException(
					"Unable to initialize Component. Failure while invoking method " + methodName
							+ ". Cause: " + ex);
		}
	}
}
