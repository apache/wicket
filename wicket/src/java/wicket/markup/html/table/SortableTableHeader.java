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

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.html.border.Border;
import wicket.markup.html.link.Link;


/**
 * Sortable table header component. Functionality provided includes
 * sorting the underlying table and changing the colours etc. (the style)
 * of the header.
 *
 * @author Juergen Donnerstag
 */
public abstract class SortableTableHeader extends Border
{ // TODO finalize javadoc
    /** The group, the table header belongs to */
    private final SortableTableHeaderGroup group;

    /** Sort ascending or descending */
    private boolean ascending;

    /**
     * Constructor.
     *
     * @param componentName The name of this component
     * @param group
     */
    public SortableTableHeader(final String componentName,
            final SortableTableHeaderGroup group)
    {
        super(componentName);

        // Default to descending. Next/first linkClick will make it ascending
        this.ascending = false;
        this.group = group;

        final SortableTableHeader me = this;

        // Action link to define a linkClicked action
        add(new Link("actionLink")
        {
            public void linkClicked(final RequestCycle cycle)
            {
                // call SortableTableHeaders implementation
                me.linkClicked(cycle);

                // Redirect back to result to avoid refresh updating the link count
                cycle.setRedirect(true);
            }
        });
    }

    /**
     * Header has been clicked. Define what to do.
     *
     * @param cycle
     */
    protected void linkClicked(final RequestCycle cycle)
    {
        // change sorting order: ascending <-> descending
        ascending = !ascending;

        // Tell the header group that something has changed
        group.setSortedColumn(getName());

        // sort the table's model data accordingly
        sort();

        // Redirect back to result to avoid refresh updating the link count
        cycle.setRedirect(true);
    }

    /**
     * Get CSS style for the header
     *
     * @return css class
     */
    protected final String getCssClass()
    {
        return group.getCssClass(getName(), ascending);
    }

    /**
     * Handle the component's tag
     *
     * @see wicket.Component#handleComponentTag(wicket.RequestCycle,
     *      wicket.markup.ComponentTag)
     */
    protected void handleComponentTag(RequestCycle cycle, ComponentTag tag)
    {
        group.handleComponentTag(cycle, tag, getCssClass());
    }
    
    /**
     * Compare two object (list elements of table's model object). Both
     * objects must implement Comparable. In order to compare basic
     * types like int or double, subclasses may override this method.
     *
     * @param o1
     * @param o2
     * @return compare result
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
     * @param object
     * @return comparable object
     */
    protected Comparable getObjectToCompare(Object object)
    {
        return (Comparable) object;
    }

    /**
     * Sort table's model object
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


