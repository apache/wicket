/*
 * Created on 27.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
