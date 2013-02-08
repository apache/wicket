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
package org.apache.wicket.util.tester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import org.apache.wicket.Component;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * A <code>WicketTester</code>-specific helper class.
 * 
 * @author Ingram Chen
 * @since 1.2.6
 */
public class WicketTesterHelper
{
	/**
	 * <code>ComponentData</code> class.
	 * 
	 * @author Ingram Chen
	 * @since 1.2.6
	 */
	public static class ComponentData implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		/** Component path. */
		public String path;

		/** Component type. */
		public String type;

		/** Component value. */
		public String value;
	}

	/**
	 * Gets recursively all <code>Component</code>s of a given <code>Page</code>, extracts the
	 * information relevant to us, and adds them to a <code>List</code>.
	 * 
	 * @param page
	 *            the <code>Page</code> to analyze
	 * @return a <code>List</code> of <code>Component</code> data objects
	 */
	public static List<WicketTesterHelper.ComponentData> getComponentData(final Page page)
	{
		final List<ComponentData> data = new ArrayList<ComponentData>();

		if (page != null)
		{
			page.visitChildren(new IVisitor<Component, Void>()
			{
				@Override
				public void component(final Component component, final IVisit<Void> visit)
				{
					final ComponentData object = new ComponentData();

					// anonymous class? Get the parent's class name
					String name = component.getClass().getName();
					if (name.indexOf("$") > 0)
					{
						name = component.getClass().getSuperclass().getName();
					}

					// remove the path component
					name = Strings.lastPathComponent(name, Component.PATH_SEPARATOR);

					object.path = component.getPageRelativePath();
					object.type = name;
					try
					{
						object.value = component.getDefaultModelObjectAsString();
					}
					catch (Exception e)
					{
						object.value = e.getMessage();
					}

					data.add(object);
				}
			});
		}
		return data;
	}

	/**
	 * Asserts that both <code>Collection</code>s contain the same elements.
	 * 
	 * @param expects
	 *            a <code>Collection</code> object
	 * @param actuals
	 *            a <code>Collection</code> object
	 */
	public static void assertEquals(final Collection<?> expects, final Collection<?> actuals)
	{
		if ((actuals.size() != expects.size()) || !expects.containsAll(actuals) ||
			!actuals.containsAll(expects))
		{
			failWithVerboseMessage(expects, actuals);
		}
	}

	/**
	 * Fails with a verbose error message.
	 * 
	 * @param expects
	 *            a <code>Collection</code> object
	 * @param actuals
	 *            a <code>Collection</code> object
	 */
	public static void failWithVerboseMessage(final Collection<?> expects,
		final Collection<?> actuals)
	{
		Assert.fail("\nexpect (" + expects.size() + "):\n" + asLined(expects) + "\nbut was (" +
			actuals.size() + "):\n" + asLined(actuals));
	}

	/**
	 * A <code>toString</code> method for the given <code>Collection</code>.
	 * 
	 * @param objects
	 *            a <code>Collection</code> object
	 * @return a <code>String</code> representation of the <code>Collection</code>
	 */
	public static String asLined(final Collection<?> objects)
	{
		StringBuilder lined = new StringBuilder();
		for (Iterator<?> iter = objects.iterator(); iter.hasNext();)
		{
			String objectString = iter.next().toString();
			lined.append("   ");
			lined.append(objectString);
			if (iter.hasNext())
			{
				lined.append("\n");
			}
		}
		return lined.toString();
	}

	/**
	 * Finds the first AjaxEventBehavior attached to the specified component with the
	 * specified event.
	 *
	 * @param component
	 * @param event
	 * @return the first behavior for this event, or {@code null}
	 */
	public static AjaxEventBehavior findAjaxEventBehavior(Component component, String event)
	{
		List<AjaxEventBehavior> behaviors = findAjaxEventBehaviors(component, event);
		AjaxEventBehavior behavior = null;
		if (behaviors != null && behaviors.isEmpty() == false)
		{
			behavior = behaviors.get(0);
		}
		return behavior;
	}

	/**
	 * Finds all AjaxEventBehavior's attached  to the specified component with
	 * the specified event.
	 *
	 * @param component
	 * @param event
	 * @return a list of all found AjaxEventBehavior or an empty list
	 */
	public static List<AjaxEventBehavior> findAjaxEventBehaviors(Component component, String event)
	{
		Args.notEmpty(event, "event");
		List<AjaxEventBehavior> behaviors = new ArrayList<AjaxEventBehavior>();
		String[] eventNames = Strings.split(event, ' ');
		for (String eventName : eventNames)
		{
			if (eventName.startsWith("on"))
			{
				eventName = eventName.substring(2);
			}

			for (Behavior behavior : component.getBehaviors())
			{
				if (behavior instanceof AjaxEventBehavior)
				{
					String behaviorEvent = ((AjaxEventBehavior)behavior).getEvent();
					String[] behaviorEventNames = Strings.split(behaviorEvent, ' ');
					for (String behaviorEventName : behaviorEventNames)
					{
						if (behaviorEventName.startsWith("on"))
						{
							behaviorEventName = behaviorEventName.substring(2);
						}
						if (eventName.equalsIgnoreCase(behaviorEventName))
						{
							behaviors.add((AjaxEventBehavior)behavior);
						}
					}
				}
			}
		}
		return behaviors;
	}

	/**
	 * @param component
	 * @param behaviorClass
	 * @return Behavior or null
	 */
	public static Behavior findBehavior(Component component, Class<? extends Behavior> behaviorClass)
	{
		for (Behavior behavior : component.getBehaviors())
		{
			if (behaviorClass.isAssignableFrom(behavior.getClass()))
			{
				return behavior;
			}
		}
		return null;
	}

}
