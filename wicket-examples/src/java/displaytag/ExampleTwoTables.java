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

import java.util.List;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;

import displaytag.utils.TestList;

/**
 * Two independent sortable + pageable tables on the same page
 * 
 * @author Juergen Donnerstag
 */
public class ExampleTwoTables extends HtmlPage
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExampleTwoTables(final PageParameters parameters)
    {
        final List data = new TestList(60, false);

        add(new SortablePageableDisplaytagTableComponent("table1", data));
        add(new SortablePageableDisplaytagTableComponent("table2", data));
    }
}