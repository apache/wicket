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

import java.util.List;

import wicket.Container;
import wicket.markup.ComponentTag;
import wicket.util.collections.MicroMap;

/**
 * Maintains a group of sortable table headers. By means of this group 
 * you change and maintain the information about which column shall be 
 * sorted. It also provides support for the table to change the
 * style of the header depending on its status. 
 *
 * @author Juergen Donnerstag
 */
public class SortableTableHeaderGroup
{ // TODO finalize javadoc
    /** contains the name of SortableTableHeader to be sorted */
    final private MicroMap sorted = new MicroMap();

    /** The Table tableComponentName refers to */
    private ListView table;

    /** The html container the header must be added to */
    private Container container;

    /** Automatically add the markup for each Link required */
    private boolean addActionLinkMarkup = false;

    /**
     * Maintain a group SortableTableHeader
     *
     * @param container The html container the header will be added to
     * @param listView The underlying ListView
     */
    public SortableTableHeaderGroup(final Container container, final ListView listView)
    {
        this.table = listView;
        this.container = container;
    }

    /**
     * Set the column to be sorted
     * @param name SortableTableHeader component name
     */
    protected final void setSortedColumn(final String name)
    {
        sorted.clear();
        sorted.put(name, null);
        
        table.invalidateModel();
    }

    /**
     * True if column with name shall be sorted
     *
     * @param name
     * @return True, if column must be sorted
     */
    protected final boolean isSorted(final String name)
    {
        return sorted.containsKey(name);
    }

    /**
     * Get the table's model data
     *
     * @return the table's underlying list
     */
    protected List getTableModelObject()
    {
        return table.getList();
    }

    /**
     * Get CSS style for a header. May be subclassed for company standards
     *
     * @param name The headers component name
     * @param ascending Sorting order
     * @return The CSS style to be applied to the tag's class attribute
     */
    protected final String getCssClass(final String name, final boolean ascending)
    {
        if (isSorted(name))
        {
            return (ascending ? "order2" : "order1") + " sortable sorted";
        }

        return null;
    }

    /**
     * Called by SortableTableHeader and may be subclassed for company standards.
     * @param tag
     * @param style
     */
    protected void handleComponentTag(final ComponentTag tag, final String style)
    {
        if (style != null)
        {
            tag.put("class", style);
        }
    }

    /**
     *
     * @return If true, actionLink markup will be added automatically
     */
    protected boolean isAddActionLinkMarkup()
    {
        return addActionLinkMarkup;
    }

    /**
     * If true, actionLink markup will be added automatically
     * @param addActionLinkMarkup
     */
    protected void setAddActionLinkMarkup(boolean addActionLinkMarkup)
    {
        this.addActionLinkMarkup = addActionLinkMarkup;
    }
}
