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
package wicket.markup.html.form;


import java.io.Serializable;

import java.util.Collection;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;

/**
 * Essentially a drop down choice that doesn't drop down. Instead, it scrolls and displays
 * a given number of rows.
 * @author Jonathan Locke
 */
public final class ListChoice extends DropDownChoice implements FormComponent.ICookieValue
{
    /** Serial Version ID */
	private static final long serialVersionUID = 1227773600645861006L;

	// The default maximum row value
    private static int defaultMaxRows = 8;

    // The maximum number of rows to show
    private int maxRows;

    /**
     * Constructor
     * @param componentName The component name
     * @param model The model
     * @param values The values to select from
     */
    public ListChoice(final String componentName, final Serializable model, final Collection values)
    {
        this(componentName, model, values, defaultMaxRows);
    }

    /**
     * Constructor
     * @param componentName The component name
     * @param model The model
     * @param values The values to select from
     * @param maxRows The maximum number of rows to display
     */
    public ListChoice(final String componentName, final Serializable model,
            final Collection values, final int maxRows)
    {
        super(componentName, model, values);
        this.maxRows = maxRows;
        setRenderNullOption(false);
    }

    /**
     * @see wicket.Component#handleComponentTag(RequestCycle, ComponentTag)
     */
    protected void handleComponentTag(final RequestCycle cycle, final ComponentTag tag)
    {
        super.handleComponentTag(cycle, tag);
        tag.put("size", Math.min(maxRows, getValues().size()));
    }
    
    /**
     * @return Returns the defaultMaxRows.
     */
    public static int getDefaultMaxRows()
    {
        return defaultMaxRows;
    }

    /**
     * @param defaultMaxRows The defaultMaxRows to set.
     */
    public static void setDefaultMaxRows(final int defaultMaxRows)
    {
        ListChoice.defaultMaxRows = defaultMaxRows;
    }
}

///////////////////////////////// End of File /////////////////////////////////
