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

import com.voicetribe.wicket.Container;
import com.voicetribe.wicket.IModel;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.panel.Panel;
import com.voicetribe.wicket.markup.html.table.Cell;
import com.voicetribe.wicket.markup.html.table.Table;

import displaytag.utils.MyTable;

/**
 * This is a convinient component to create sorted table headers very easily.
 * It first scans the markup for &lt;th id="wcn-.*" ..&gt> tags and 
 * automatically create a SimpleHrefComponent for each.
 * <p>
 * The component can only be used with &lt;thead&gt; tags. 
 * 
 * @author Juergen Donnerstag
 */
public class TableGeneratorComponent extends Panel
{
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

    private final void init(final List data, final List headers, final List columns)
    {
        add(new Table("headers", headers)
        {
            protected boolean populateCell(Cell cell)
            {
                Object header = headers.get(cell.getIndex());
                if (populateHeader(cell, header) == false)
                {
                    String value = (String)(header instanceof String ? header : ((IModel)header).getObject());
                    cell.add(new Label("header", value));
                }
                
                return true;
            }
        });
        
        add(new MyTable("rows", data)
        {
            protected boolean populateCell(final Cell rowCell, final Container tagClass)
            {
                tagClass.add(new Table("columns", columns)
                {
                    protected boolean populateCell(final Cell colCell)
                    {
                        Object column = columns.get(colCell.getIndex());
                        if (populateColumn(colCell, column) == false)
                        {
                            String value = (String)(column instanceof String ? column : ((IModel)column).getObject());
                            colCell.add(new Label("column", rowCell.getModel(), value));
                        }
                        
                        return true;
                    }
                });
                
                return true;
            }
        });
    }
    
    protected boolean populateHeader(final Cell cell, final Object header)
    {
        return false;
    }
    
    protected boolean populateColumn(final Cell cell, final Object column)
    {
        return false;
    }
}
