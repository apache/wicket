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

import displaytag.utils.TestList;

/**
 * Show different means of displaying subsets of a table
 * 
 * @author Juergen Donnerstag
 */
public class ExampleSubsets extends Displaytag
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
        // Because subList() returns a view (not a copy) it is not serializable
        // and thus can not be used directly.
        List data2 = new ArrayList();
        data2.addAll(data.subList(0, 5));
        add(new SimpleDisplaytagTableComponent("table2", data2));
        
        // Second alternativ
        SimpleDisplaytagTableComponent table = new SimpleDisplaytagTableComponent("table3", data);
        table.setStartIndex(3);
        table.setViewSize(8 - 3);
        add(table);
    }
}