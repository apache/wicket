/*
 * Created on 27.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
        HtmlContainer hc = new HtmlContainer("class");
        hc.addAttributeModifier(
                new ComponentTagAttributeModifier("class",
                                                  new Model(cell.isEvenIndex() ? "even" : "odd")));
        cell.add(hc);

        populateCell(cell, hc);
    }

    protected abstract boolean populateCell(final Cell cell, final Container tagClass);
}
