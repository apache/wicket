/*
 * $Id: WicketTesterHelper.java 5771 2006-05-19 12:04:06 +0000 (Fri, 19 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-19 12:04:06 +0000 (Fri, 19
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
package wicket.util.tester;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import wicket.Component;
import wicket.Page;
import wicket.Component.IVisitor;
import wicket.util.string.Strings;

/**
 * A WicketTester specific helper class
 * 
 * @author Ingram Chen
 */
public class WicketTesterHelper
{
	/**
	 * 
	 */
	public static class ComponentData implements Serializable
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
	 * Get recursively all components of the page, extract the information
	 * relevant for us and add them to a list.
	 * 
	 * @param page
	 * @return List of component data objects
	 */
	public static List<ComponentData> getComponentData(final Page page)
	{
		final List<ComponentData> data = new ArrayList<ComponentData>();

		page.visitChildren(new IVisitor()
		{
			public Object component(final Component component)
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
					object.value = component.getModelObjectAsString();
				}
				catch (Exception e)
				{
					object.value = e.getMessage();
				}

				data.add(object);
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});

		return data;
	}

	/**
	 * Assert both collections contain the same elements
	 * 
	 * @param expects
	 * @param actuals
	 */
	public static void assertEquals(final Collection<?> expects,
			final Collection<?> actuals)
	{
		if (!expects.containsAll(actuals) || !actuals.containsAll(expects))
		{
			failWithVerboseMessage(expects, actuals);
		}
	}

	/**
	 * Fail with a verbose error message
	 * 
	 * @param expects
	 * @param actuals
	 */
	public static void failWithVerboseMessage(final Collection expects, final Collection actuals)
	{
		Assert.fail("\nexpect (" + expects.size() + "):\n" + asLined(expects) + "\nbut was ("
				+ actuals.size() + "):\n" + asLined(actuals));
	}

	/**
	 * toString() for the collection provided.
	 * 
	 * @param objects
	 * @return String
	 */
	public static String asLined(final Collection objects)
	{
		StringBuffer lined = new StringBuffer();
		for (Iterator iter = objects.iterator(); iter.hasNext();)
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
}
