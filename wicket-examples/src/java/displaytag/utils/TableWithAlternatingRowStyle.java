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
import com.voicetribe.wicket.Container;
import com.voicetribe.wicket.Model;
import com.voicetribe.wicket.markup.ComponentTagAttributeModifier;
import com.voicetribe.wicket.markup.html.HtmlContainer;
import com.voicetribe.wicket.markup.html.table.Cell;
import com.voicetribe.wicket.markup.html.table.Table;

/**
 * This is a simple Table extension providing alternate row styles (colours). 
 * The styles are named "even" and "odd".
 * 
 * Pre-requiste: The HTML markup references a Wicket component named "class"
 * 
 * @author Juergen Donnerstag
 */
public abstract class TableWithAlternatingRowStyle extends Table
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
     * Besides default behaviour, add a modifier, which will handle the
     * alternate styles.
     * TODO just an idea. Why not do during render time, avoiding the need for
     *   an extra component for each row????
     * @see com.voicetribe.wicket.markup.html.table.Table#populateCell(com.voicetribe.wicket.markup.html.table.Cell)
     */
    public void populateCell(final Cell cell)
    {
        //HtmlContainer hc = new HtmlContainer("class");
        cell.addAttributeModifier(
                new ComponentTagAttributeModifier(
                        "class",
                        new Model(cell.isEvenIndex() ? "even" : "odd")));
        
        //cell.add(hc);
        populateCell(cell, cell);
    }

    protected abstract boolean populateCell(final Cell cell, final Container tagClass);
}
