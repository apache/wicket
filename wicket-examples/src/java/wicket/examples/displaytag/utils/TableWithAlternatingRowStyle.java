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
package wicket.examples.displaytag.utils;

import java.io.Serializable;
import java.util.List;

import wicket.markup.ComponentTag;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;


/**
 * This is a simple Table extension providing alternate row styles (colours). 
 * The styles are named "even" and "odd".
 * 
 * @author Juergen Donnerstag
 */
public abstract class TableWithAlternatingRowStyle extends ListView
{
    /**
     * Constructor
     * 
     * @param componentName
     * @param data
     */
    public TableWithAlternatingRowStyle(final String componentName, final List data)
    {
        super(componentName, data);
    }

    /**
     * Subclass Table's newCell() and return a ListItem which will add/modify its
     * class attribute and thus provide ListItems with alternating row colours.
     * 
     * See wicket.markup.html.table.Table#newItem(int)
     * @param index Index of item 
     * @return List item
     */
    protected ListItem newItem(final int index)
    {
        // Make sure the model object is serializable
        Object listItem = getList().get(index);
        if (!(listItem instanceof Serializable))
        {
            throw new IllegalArgumentException("Table and table cell model data must be serializable");
        }

        // Return an extended Cell, which will automatically change the CSS style with
        // every other Cell.
        return new ListItem(this, index)
        {
            protected void onComponentTag(final ComponentTag tag)
            {
                // add/modify the attribute controlling the CSS style
                tag.put("class", this.isEvenIndex() ? "even" : "odd");
                
                // continue with default behaviour
                super.onComponentTag(tag);
            }
        };
    }
}
