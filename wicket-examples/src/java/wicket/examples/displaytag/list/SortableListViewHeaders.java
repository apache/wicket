/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.displaytag.list;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.IComponentResolver;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.list.ListView;

/**
 * This is a convenient component to create sorted list view headers very easily. It first
 * scans the markup for &lt;th wicket:id=".*" ..&gt> tags and automatically creates a
 * SortableListViewHeader for each.
 * <p>
 * The component can only be used with &lt;thead&gt; tags.
 * @see SortableListViewHeaderGroup
 * @see SortableListViewHeader
 * @author Juergen Donnerstag
 */
public class SortableListViewHeaders extends WebMarkupContainer implements IComponentResolver
{
	/** Logging. */
	final private Log log = LogFactory.getLog(SortableListViewHeaders.class);
	
	/** Each SortableTableHeader (without 's) must be attached to a group. */
	final private SortableListViewHeaderGroup group;

	/**
	 * Construct.
	 * @param componentName The component name; must not be null
	 * @param listView the underlying ListView
	 */
	public SortableListViewHeaders(final String componentName, final ListView listView)
	{
		super(componentName);

		this.group = new SortableListViewHeaderGroup(this, listView);
	}

	/**
	 * Compare two object of the column to be sorted, assuming both Objects support
	 * compareTo().
	 * @see Comparable#compareTo(java.lang.Object)
	 * @param header
	 * @param o1
	 * @param o2
	 * @return compare result
	 */
	protected int compareTo(final SortableListViewHeader header, final Object o1, final Object o2)
	{
		Comparable obj1 = getObjectToCompare(header, o1);
		Comparable obj2 = getObjectToCompare(header, o2);
		return obj1.compareTo(obj2);
	}

	/**
	 * Get one of the two Object to be compared for sorting a column.
	 * @param header
	 * @param object
	 * @return comparable object
	 */
	protected Comparable getObjectToCompare(final SortableListViewHeader header, final Object object)
	{
		return (Comparable)object;
	}

	/**
	 * 
	 * @param container
	 * @param markupStream
	 * @param tag
	 * @return true, if component got resolved
	 */
	public boolean resolve(wicket.MarkupContainer container,MarkupStream markupStream,ComponentTag tag)
	{
		if (tag.getName().equalsIgnoreCase("th"))
		{
			// Get component name
			final String componentName = tag.getId();
			if ((componentName != null) && (get(componentName) == null))
			{
				autoAdd(new SortableListViewHeader(componentName, group)
				{
					protected int compareTo(final Object o1, final Object o2)
					{
						return SortableListViewHeaders.this.compareTo(this, o1, o2);
					}

					protected Comparable getObjectToCompare(final Object object)
					{
						return SortableListViewHeaders.this.getObjectToCompare(this, object);
					}
				});
				return true;
			}
		}
		
	    return false;
	}
	
	/**
	 * Scan the related markup and attach a SortableListViewHeader to each &lt;th&gt; tag
	 * found.
	 * @see wicket.Component#onRender()
	 */
	protected void onRender()
	{
		// Get the markup related to the component
		MarkupStream markupStream = this.findMarkupStream();

		// Must be <thead> tag
		ComponentTag tag = markupStream.getTag();
		final ComponentTag openTag = tag;
		checkComponentTag(tag, "thead");

		// Continue with default behaviour
		super.onRender();
	}
}
