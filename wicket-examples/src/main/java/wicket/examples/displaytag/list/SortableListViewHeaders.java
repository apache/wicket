/*
 * $Id: SortableListViewHeaders.java 5855 2006-05-25 17:26:47 +0000 (Thu, 25 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-25 17:26:47 +0000 (Thu, 25
 * May 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.displaytag.list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.list.ListView;
import wicket.markup.resolver.IComponentResolver;

/**
 * This is a convenient component to create sorted list view headers very
 * easily. It first scans the markup for &lt;th wicket:id=".*" ..&gt> tags and
 * automatically creates a SortableListViewHeader for each.
 * <p>
 * The component can only be used with &lt;thead&gt; tags.
 * 
 * @see SortableListViewHeaderGroup
 * @see SortableListViewHeader
 * 
 * @author Juergen Donnerstag
 * 
 * @param <T>
 *            Type of model object this component holds
 */
public class SortableListViewHeaders<T> extends WebMarkupContainer<T> implements IComponentResolver
{
	/** Logging. */
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SortableListViewHeaders.class);

	/** Each SortableTableHeader (without 's) must be attached to a group. */
	final private SortableListViewHeaderGroup<T> group;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 *            The component's id; must not be null
	 * @param listView
	 *            the underlying ListView
	 */
	public SortableListViewHeaders(MarkupContainer parent, final String id, final ListView<T> listView)
	{
		super(parent, id);

		this.group = new SortableListViewHeaderGroup<T>(this, listView);
	}

	/**
	 * Compare two object of the column to be sorted, assuming both Objects
	 * support compareTo().
	 * 
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
	 * 
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
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		if (tag.getName().equalsIgnoreCase("th"))
		{
			// Get component name
			String componentId = tag.getId();
			if ((componentId != null) && !componentId.startsWith(Component.AUTO_COMPONENT_PREFIX))
			{
				componentId = Component.AUTO_COMPONENT_PREFIX + componentId;
				tag.setId(componentId);
			}
			if ((componentId != null) && (get(componentId) == null))
			{
				SortableListViewHeader<T> slvh = new SortableListViewHeader<T>(this, componentId, group)
				{
					@Override
					protected int compareTo(final Object o1, final Object o2)
					{
						return SortableListViewHeaders.this.compareTo(this, o1, o2);
					}

					@Override
					protected Comparable getObjectToCompare(final Object object)
					{
						return SortableListViewHeaders.this.getObjectToCompare(this, object);
					}
				};
				slvh.render(markupStream);
				return true;
			}
		}

		return false;
	}

	/**
	 * Scan the related markup and attach a SortableListViewHeader to each
	 * &lt;th&gt; tag found.
	 * 
	 * @see wicket.Component#onRender(MarkupStream)
	 */
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		// Must be <thead> tag
		ComponentTag tag = markupStream.getTag();
		checkComponentTag(tag, "thead");

		// Continue with default behavior
		super.onRender(markupStream);
	}
}
