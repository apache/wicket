/*
 * Created on 27.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package displaytag.utils;

import java.util.List;

import com.voicetribe.wicket.Container;
import com.voicetribe.wicket.markup.html.style.CascadingStyleSheetStyle;
import com.voicetribe.wicket.markup.html.table.Cell;
import com.voicetribe.wicket.markup.html.table.Table;

/**
 * Table with pages.
 * @author Juergen
 */
public abstract class MyPagedTable extends Table
{
    public MyPagedTable(final String componentName, final List data, int pageSize)
    {
        super(componentName, data, pageSize);
    }
    
    public void populateCell(final Cell cell)
    {
        final CascadingStyleSheetStyle tagClass = new CascadingStyleSheetStyle("class", cell.isEvenIndex() ? "even" : "odd");
        tagClass.setEnable(true);
        cell.add(tagClass);
        
        populateCell(cell, tagClass);
    }
    
    protected abstract boolean populateCell(final Cell cell, final Container tagClass);
}
