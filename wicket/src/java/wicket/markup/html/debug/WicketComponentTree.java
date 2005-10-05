/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.debug;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import wicket.Component;
import wicket.Page;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.util.string.Strings;

/**
 * This is a simple Wicket component that displays all components of a Page in a
 * table representation. Useful for debugging.
 * <p>
 * Simply add this code to your page's contructor:
 * 
 * <pre>
 * add(new WicketComponentTree(&quot;componentTree&quot;, this));
 * </pre>
 * 
 * And this to your markup:
 * 
 * <pre>
 *  &lt;span wicket:id=&quot;componentTree&quot;/&gt;
 * </pre>
 * 
 * @author Juergen Donnerstag
 */
public final class WicketComponentTree extends Panel
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param page
	 *            The page to be analyzed
	 * @see Component#Component(String)
	 */
	public WicketComponentTree(final String id, final Page page)
	{
		super(id);
		
		// Create an empty list. It'll be filled later
		final List data = new ArrayList();

		// Name of page
		add(new Label("page", page.toString()));

		// Get the components data and fill and sort the list
		data.clear();
		data.addAll(getComponentData(page));
		Collections.sort(data, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return ((ComponentData)o1).path.compareTo(((ComponentData)o2).path);
			}
		});
		
		// Create the table containing the list the components
		add(new ListView("components", data)
		{
			private static final long serialVersionUID = 1L;
			
			/**
			 * Populate the table with Wicket elements
			 */
			protected void populateItem(final ListItem listItem)
			{
				final ComponentData cdata = (ComponentData)listItem.getModelObject();

				listItem.add(new Label("row", Integer.toString(listItem.getIndex() + 1)));
				listItem.add(new Label("path", cdata.path));
				listItem.add(new Label("type", cdata.type));
				listItem.add(new Label("model", cdata.value));
			}
		});
	}

	/**
	 * Get recursively all components of the page, extract the information
	 * relevant for us and add them to a list.
	 * 
	 * @param page
	 * @return List of component data objects
	 */
	private List getComponentData(final Page page)
	{
		final List data = new ArrayList();

		page.visitChildren(new IVisitor()
		{
			public Object component(final Component component)
			{
			    if (!component.getPath().startsWith(WicketComponentTree.this.getPath()))
			    {
					final ComponentData object = new ComponentData();
	
					// anonymous class? Get the parent's class name
					String name = component.getClass().getName();
					if (name.indexOf("$") > 0)
					{
						name = component.getClass().getSuperclass().getName();
					}
	
					// remove the path component
					name = Strings.lastPathComponent(name, ':');
	
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
			    }
			    
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});

		return data;
	}

	/**
	 * El cheapo data holder.
	 * 
	 * @author Juergen Donnerstag
	 */
	private class ComponentData implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		/** Component path. */
		public String path;

		/** Component type. */
		public String type;

		/** Component value. */
		public String value;
	}
}