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
package com.voicetribe.wicket.markup.html.table;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.voicetribe.wicket.Model;

/**
 * @author Juergen Donnerstag
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TableTest extends TestCase
{
    /**
     * 
     * @param modelListSize
     * @return
     */
    private Table createTable(final int modelListSize, final int pageSize)
    {
        ArrayList modelList = new ArrayList();
        for (int i=0; i < modelListSize; i++)
        {
            modelList.add(new Integer(i));
        }
        
        return new Table("table", new Model(modelList), pageSize)
	    {
	        protected void populateItem(final ListItem listItem)
	        {
	            ; // do nothing
	        }
	    };
    }

    public void testTable()
    {
        Table table = createTable(20, 4);
        assertEquals(4, table.getRowsPerPage());
        assertEquals(0, table.getCurrentPage());
        assertEquals(5, table.getPageCount());
        assertEquals(4, table.getViewSize());
        
        table = createTable(20, 6);
        assertEquals(6, table.getRowsPerPage());
        assertEquals(0, table.getCurrentPage());
        assertEquals(4, table.getPageCount());
        assertEquals(6, table.getViewSize());
        
        table.setCurrentPage(1);
        assertEquals(6, table.getRowsPerPage());
        assertEquals(1, table.getCurrentPage());
        assertEquals(4, table.getPageCount());
        assertEquals(6, table.getViewSize());
        assertEquals(6, table.getStartIndex());
        
        table.setCurrentPage(3);
        assertEquals(6, table.getRowsPerPage());
        assertEquals(3, table.getCurrentPage());
        assertEquals(4, table.getPageCount());
        assertEquals(2, table.getViewSize());
        assertEquals(18, table.getStartIndex());
    }
}
