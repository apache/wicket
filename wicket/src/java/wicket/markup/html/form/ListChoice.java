/*
 * $Id$ $Revision:
 * 1.8 $ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form;

import java.util.Collection;

import wicket.markup.ComponentTag;
import wicket.model.IModel;

/**
 * Essentially a drop down choice that doesn't drop down. Instead, it scrolls
 * and displays a given number of rows.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Eelco Hillenius
 */
public class ListChoice extends DropDownChoice
{
	/** The default maximum number of rows to display. */
	private static int defaultMaxRows = 8;
    
	/** Serial Version ID. */
	private static final long serialVersionUID = 1227773600645861006L;

	/** The maximum number of rows to display. */
	private int maxRows;

	/**
	 * Gets the default maximum number of rows to display.
	 * 
	 * @return Returns the defaultMaxRows.
	 */
	protected static int getDefaultMaxRows()
	{
		return defaultMaxRows;
	}

	/**
	 * Sets the default maximum number of rows to display.
	 * 
	 * @param defaultMaxRows
	 *            The defaultMaxRows to set.
	 */
	protected static void setDefaultMaxRows(final int defaultMaxRows)
	{
		ListChoice.defaultMaxRows = defaultMaxRows;
	}
	
	/**
	 * @see AbstractChoice#AbstractChoice(String, Collection)
	 */
	public ListChoice(String name, final Collection values)
	{
		super(name, values);
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param model
	 *            See Component constructor
	 * @param values
	 *            The collection of values in the list
	 * @see DropDownChoice#DropDownChoice(String, IModel, Collection)
	 */
	public ListChoice(final String name, final IModel model, final Collection values)
	{
		this(name, model, values, defaultMaxRows);
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param model
	 *            See Component constructor
	 * @param values
	 *            The collection of values in the list
	 * @param maxRows
	 *            Maximum number of rows to show
	 * @see DropDownChoice#DropDownChoice(String, IModel, Collection)
	 */
	public ListChoice(final String name, final IModel model, final Collection values, final int maxRows)
	{
		super(name, model, values);
		this.maxRows = maxRows;
	}

	/**
	 * Gets the maximum number of rows to display.
	 * 
	 * @return the maximum number of rows to display
	 */
	public final int getMaxRows()
	{
		return maxRows;
	}

	/**
	 * Sets the maximum number of rows to display.
	 * 
	 * @param maxRows
	 *            the maximum number of rows to display
	 * @return This
	 */
	public final ListChoice setMaxRows(int maxRows)
	{
		this.maxRows = maxRows;
		return this;
	}

	/**
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected final void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("size", Math.min(maxRows, getChoices().size()));
	}

    /**
     * @see wicket.markup.html.form.FormComponent#supportsPersistence()
     */
    protected final boolean supportsPersistence()
    {
        return true;
    }
}
