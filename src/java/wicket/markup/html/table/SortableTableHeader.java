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
package wicket.markup.html.table;

import java.util.Collections;
import java.util.Comparator;

import wicket.markup.ComponentTag;
import wicket.markup.html.border.Border;
import wicket.markup.html.link.Link;


/**
 * Sortable table header component for a single table column. Functionality 
 * provided includes sorting the underlying table and changing 
 * the colours (style) of the header.
 *
 * @see SortableTableHeaderGroup
 * @see SortableTableHeaders
 * 
 * @author Juergen Donnerstag
 */
public abstract class SortableTableHeader extends Border
{
    /** All sortable columns of a single table are grouped */
    private final SortableTableHeaderGroup group;

    /** Sort ascending or descending */
    private boolean ascending;

    /**
     * Construct.
     *
     * @param componentName The component name
     * @param group The group of headers the new one will be added to 
     */
    public SortableTableHeader(final String componentName,
            final SortableTableHeaderGroup group)
    {
        super(componentName);

        // Default to descending.
        this.ascending = false;
        this.group = group;

        // If user clicks on the header, sorting will reverse
        final SortableTableHeader me = this;
        add(new Link("actionLink")
        {
            public void linkClicked()
            {
                // call SortableTableHeaders implementation
                me.linkClicked();

                // Redirect back to result to avoid refresh updating the link count
                getRequestCycle().setRedirect(true);
            }
        });
    }

    /**
     * Header has been clicked. Define what to do.
     */
    protected void linkClicked()
    {
        // change sorting order: ascending <-> descending
        ascending = !ascending;

        // Tell the header group that something has changed
        group.setSortedColumn(getName());

        // sort the table's model data accordingly
        sort();

        // Redirect back to result to avoid refresh updating the link count
        getRequestCycle().setRedirect(true);
    }

    /**
     * Get CSS style for the header based on ascending / descending.
     * Delegate to the header group to determine the style.
     *
     * @return css class
     */
    protected final String getCssClass()
    {
        return group.getCssClass(getName(), ascending);
    }

    /**
     * Delegate to the header group to handle the tag.
     * @see wicket.Component#handleComponentTag(wicket.markup.ComponentTag)
     * @param tag The current ComponentTag to handle
     */
    protected void handleComponentTag(final ComponentTag tag)
    {
        group.handleComponentTag(tag, getCssClass());
    }
    
    /**
     * Compare two objects (list elements of table's model object). Both
     * objects must implement Comparable. In order to compare basic
     * types like int or double, simply subclass the method.
     *
     * @param o1 first object
     * @param o2 second object
     * @return comparision result
     */
    protected int compareTo(Object o1, Object o2)
    {
        Comparable obj1 = getObjectToCompare(o1);
        Comparable obj2 = getObjectToCompare(o2);
        return obj1.compareTo(obj2);
    }

    /**
     * Returns the comparable object of the table the header/column is
     * referring to, e.g. obj.getId();
     *
     * @param object the ListItems model object
     * @return The object to compare
     */
    protected Comparable getObjectToCompare(Object object)
    {
        return (Comparable) object;
    }

    /**
     * Sort table's model object based on column data
     */
    protected void sort()
    {
        Collections.sort(group.getTableModelObject(), new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                if (ascending)
                {
                    return compareTo(o1, o2);
                }
                else
                {
                    return compareTo(o2, o1);
                }
            }
        });
    }
}
