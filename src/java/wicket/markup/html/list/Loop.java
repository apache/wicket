/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.markup.html.list;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * 
 */
public abstract class Loop extends WebMarkupContainer
{
	/** Log. */
	private static Log log = LogFactory.getLog(Loop.class);
	
	/**
	 * @see wicket.Component#Component(String, IModel)
	 * @param id
	 * @param size
	 */
	public Loop(final String id, final int size)
	{
		super(id, new Model(new Integer(size)));
	}
	
	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public Loop(final String id, final IModel model)
	{
		super(id, model);
	}

	/**
	 * 
	 * @return size
	 */
	public int getSize()
	{
	    return ((Integer)getModelObject()).intValue();
	}
	
	/**
	 * Renders this ListView (container).
	 */
	protected void onRender()
	{
		// Ask parents for markup stream to use
		final MarkupStream markupStream = findMarkupStream();

		// Save position in markup stream
		final int markupStart = markupStream.getCurrentIndex();

		// Get number of listItems to be displayed
		final int size = getSize();
		if (size > 0)
		{
			// Loop through the markup in this container for each child
			// container
			for (int i = 0; i < size; i++)
			{
				// Get the name of the component for listItem i
				final String componentName = Integer.toString(i);

				// If this component does not already exist, populate it
				LoopItem loopItem = (LoopItem)get(componentName);
				if (loopItem == null)
				{
					// Create listItem for index i of the list
					loopItem = newItem(i);

				    onBeginPopulateItem(loopItem);
					populateItem(loopItem);

					// Add cell to list view
					add(loopItem);
				}

				// Rewind to start of markup for kids
				markupStream.setCurrentIndex(markupStart);

				// Render cell
				renderItem(loopItem, i >= (size - 1));
			}
		}
		else
		{
		    removeAll();
			markupStream.skipComponent();
		}
	}

	/**
	 * Create a new LoopItem for list item at index.
	 * 
	 * @param index
	 * @return LoopItem
	 */
	protected LoopItem newItem(final int index)
	{
		return new LoopItem(index);
	}

	/**
	 * Populate a given listItem.
	 * 
	 * @param listItem
	 *            The listItem to populate
	 */
	protected abstract void populateItem(final LoopItem listItem);

	/**
	 * Comes handy for ready made ListView based components which must implement
	 * populateItem() but you don't want to loose compile time error checking 
	 * reminding the user to implement abstract populateItem().
	 * 
	 * @param listItem
	 */
	protected void onBeginPopulateItem(final LoopItem listItem)
	{
	}

	/**
	 * Render a single loopItem.
	 * 
	 * @param loopItem
	 *            the loopItem to be rendered
	 * @param lastItem
	 *            True, if item is last loopItem in loopView
	 */
	protected void renderItem(final LoopItem loopItem, final boolean lastItem)
	{
		loopItem.render();
	}
}