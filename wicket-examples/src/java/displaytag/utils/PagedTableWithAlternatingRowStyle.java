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
package displaytag.utils;

import java.util.List;

import com.voicetribe.wicket.Model;
import com.voicetribe.wicket.markup.ComponentTagAttributeModifier;
import com.voicetribe.wicket.markup.html.table.ListItem;
import com.voicetribe.wicket.markup.html.table.Table;

/**
 * Paged table with alternating row styles
 * 
 * @author Juergen
 */
public abstract class PagedTableWithAlternatingRowStyle extends Table
{
    /**
     * Constructor
     * 
     * @param componentName
     * @param data
     * @param pageSize
     */
    public PagedTableWithAlternatingRowStyle(final String componentName, final List data, int pageSize)
    {
        super(componentName, data, pageSize);
    }


    /**
     * Change the style with every other row
     * 
     * @see com.voicetribe.wicket.markup.html.table.Table#populateCell(com.voicetribe.wicket.markup.html.table.Cell)
     */
    protected void populateItem(final ListItem listItem)
    {
        listItem.addAttributeModifier(
                new ComponentTagAttributeModifier(
                        "class",
                        new Model(listItem.isEvenIndex() ? "even" : "odd")));
        
    }
}
