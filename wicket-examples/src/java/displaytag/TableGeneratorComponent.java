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

import com.voicetribe.wicket.IModel;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.panel.Panel;
import com.voicetribe.wicket.markup.html.table.ListItem;
import com.voicetribe.wicket.markup.html.table.ListView;

import displaytag.utils.TableWithAlternatingRowStyle;

/**
 * This is a convinient component to dynamically create tables. It takes
 * the header and the tables data, and the rest is magic. 
 * 
 * @author Juergen Donnerstag
 */
public class TableGeneratorComponent extends Panel
{
    /**
     * Constructor
     * 
     * @param componentName
     * @param data
     * @param headers
     * @param columns
     */
    public TableGeneratorComponent(final String componentName, final List data, final String[] headers, final String[] columns)
    {
        super(componentName);

        final List headerList = new ArrayList(headers.length);
        for (int i=0; i < headers.length; i++)
        {
            headerList.add(headers[i]);
        }

        List columnList = headerList;
        if ((columns != null) && (headers != columns))
        {
            columnList = new ArrayList(columns.length);
            for (int i=0; i < columns.length; i++)
            {
                columnList.add(columns[i]);
            }
        }
        
        init(data, headerList, columnList);
    }
    
    /**
     * Constructor
     * 
     * @param componentName The component name; must not be null
     * @param data The tables underlying model object list
     * @param tableComponentName The component name of the Table
     */
    public TableGeneratorComponent(final String componentName, final List data, final List headers, final List columns)
    {
        super(componentName);
        
        init(data, headers, columns);
    }

    /**
     * Initialite the Component
     * 
     * @param data
     * @param headers
     * @param columns
     */
    private final void init(final List data, final List headers, final List columns)
    {
        // Add table header
        add(new ListView("headers", headers)
        {
            protected void populateItem(final ListItem listItem)
            {
                Object header = headers.get(listItem.getIndex());
                if (populateHeader(listItem, header) == false)
                {
                    String value = (String)(header instanceof String ? header : ((IModel)header).getObject());
                    listItem.add(new Label("header", value));
                }
            }
        });
        
        // Add table rows
        add(new TableWithAlternatingRowStyle("rows", data)
        {
            protected void populateItem(final ListItem rowItem)
            {
                rowItem.add(new ListView("columns", columns)
                {
                    protected void populateItem(final ListItem colItem)
                    {
                        Object column = columns.get(colItem.getIndex());
                        if (populateColumn(colItem, column) == false)
                        {
                            String value = (String)(column instanceof String ? column : ((IModel)column).getObject());
                            colItem.add(new Label("column", rowItem.getModel(), value));
                        }
                    }
                });
            }
        });
    }

    /**
     * To be subclassed
     * 
     * @param cell
     * @param header
     * @return
     */
    protected boolean populateHeader(final ListItem listItem, final Object header)
    {
        return false;
    }
    
    /**
     * To be subclassed
     * 
     * @param cell
     * @param column
     * @return
     */
    protected boolean populateColumn(final ListItem listItem, final Object column)
    {
        return false;
    }
}
