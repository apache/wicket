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
package displaytag;

import java.util.ArrayList;
import java.util.List;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;

import displaytag.utils.TestList;

/**
 * Start page for different displaytag pages
 * 
 * @author Juergen Donnerstag
 */
public class ExampleSubsets extends HtmlPage
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExampleSubsets(final PageParameters parameters)
    {
        List data = new TestList(10, false);
        
        // Add table of existing comments
        add(new SimpleDisplaytagTableComponent("table1", data));

        // First alternativ
        List data2 = new ArrayList();
        data2.addAll(data.subList(0, 5));
        add(new SimpleDisplaytagTableComponent("table2", data2));

        // Second alternativ
/* TODO Currently broken, because functionality has been removed from Table        
        SimpleDisplaytagTableComponent table = new SimpleDisplaytagTableComponent("table3", data);
        table.setStartIndex(3);
        table.setNumberOfCellsToDisplay(8 - 3);
        add(table);
*/        
    }
}