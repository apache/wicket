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
package wicket.markup.html.table;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlContainer;

/**
 * This is a convenient component to create sorted table headers very easily.
 * It first scans the markup for &lt;th id="wicket-.*" ..&gt> tags and 
 * automatically creates a SortableTableHeader for each.
 * <p>
 * The component can only be used with &lt;thead&gt; tags. 
 * 
 * @author Juergen Donnerstag
 */
public class SortableTableHeaders extends HtmlContainer
{ // TODO finalize javadoc
    /** Logging */
    final private Log log = LogFactory.getLog(SortableTableHeaders.class);
    
    /** Each SortableTableHeader (without 's)  must be related to a group */
    final private SortableTableHeaderGroup group;

    /**
     * Constructor
     * 
     * @param componentName The component name; must not be null
     * @param listView the list view
     * @param addActionLinkMarkup
     */
    public SortableTableHeaders(final String componentName,
            final ListView listView, final boolean addActionLinkMarkup)
    {
        super(componentName);
        
        this.group = new SortableTableHeaderGroup(this, listView);
        this.group.setAddActionLinkMarkup(addActionLinkMarkup);
    }

    /**
     * Scan the related markup and attach a SortableTableHeader to each 
     * &lt;th&gt; tag found.
     * @see wicket.Component#handleRender()
     */
    protected void handleRender()
    {
        // Allow anonmous class to access 'this' methods with same name
        final SortableTableHeaders me = this;

        // Get the markup related to the component
        MarkupStream markupStream = this.findMarkupStream();
        
        // Save position in markup stream
        final int markupStart = markupStream.getCurrentIndex();

        // Must be <thead> tag
        ComponentTag tag = markupStream.getTag();
        checkTag(tag, "thead");
        
        // find all <th id="wicket-..." childs
        // Loop through the markup in this container
        markupStream.next();
        while (markupStream.hasMore())
        {
            final MarkupElement element = markupStream.get();
            if (element instanceof ComponentTag)
            {
                // Get element as tag
                tag = (ComponentTag) element;
                if (tag.getName().equalsIgnoreCase("th"))
                {
                    // Get component name
                    final String componentName = tag.getComponentName();
                    if ((componentName != null) && (get(componentName) == null))
                    {
	                    add(new SortableTableHeader(componentName, group)
	                    {
	                        protected int compareTo(final Object o1, final Object o2)
	                        {
	                            return me.compareTo(this, o1, o2);
	                        }
	
	                        protected Comparable getObjectToCompare(final Object object)
	                        {
	                            return me.getObjectToCompare(this, object);
	                        }
	                    });
                    }
                }
            }
            
            markupStream.next();
        }
        
        // Rewind to start of markup
        markupStream.setCurrentIndex(markupStart);

        // Continue with default behaviour
        super.handleRender();
    }

    /**
     * Compare two object of the column to be sorted, assuming both Objects
     * support compareTo().
     * 
     * @see Comparable#compareTo(java.lang.Object)
     * 
     * @param header
     * @param o1
     * @param o2
     * @return compare result
     */
    protected int compareTo(final SortableTableHeader header, final Object o1, final Object o2)
    {
        Comparable obj1 = getObjectToCompare(header, o1);
        Comparable obj2 = getObjectToCompare(header, o2);
        return obj1.compareTo(obj2);
    }

    /**
     * Get one of the two Object to be compared for sorting a column.
     * 
     * @param header
     * @param object
     * @return comparable object
     */
    protected Comparable getObjectToCompare(final SortableTableHeader header, final Object object)
    {
        return (Comparable) object;
    }
}
