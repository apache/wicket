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
 * This is a very basic loop component. It's model is simply a size value. During
 *  render phase it iterates from 0 to size - 1, creates a new LoopItem for each
 *  index, populates and renders it.
 * 
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public abstract class Loop extends WebMarkupContainer
{
	/** Log. */
	private static Log log = LogFactory.getLog(Loop.class);
	
	/**
	 * Construct.
	 * 
	 * @see wicket.Component#Component(String, IModel)
	 * @param id component id
	 * @param size max index of the loop
	 */
	public Loop(final String id, final int size)
	{
		super(id, new Model(new Integer(size)));
	}
	
	/**
	 * Construct.
	 * 
	 * @see wicket.Component#Component(String, IModel)
	 * 
	 * @param id component id
	 * @param model must contain a Integer model object
	 */
	public Loop(final String id, final IModel model)
	{
		super(id, model);
	}

	/**
	 * The size of the loop
	 * 
	 * @return size
	 */
	public int getSize()
	{
	    return ((Integer)getModelObject()).intValue();
	}
	
	/**
	 * Renders this Loop (container).
	 */
	protected void onRender()
	{
		// Ask parents for markup stream to use
		final MarkupStream markupStream = findMarkupStream();

		// Save position in markup stream
		final int markupStart = markupStream.getCurrentIndex();

		// Get number of loopItems to be displayed
		final int size = getSize();
		if (size > 0)
		{
			// Loop through the markup in this container for each child
			// container
			for (int i = 0; i < size; i++)
			{
				// Get the name of the component for loopItem i
				final String componentName = Integer.toString(i);

				// If this component does not already exist, populate it
				LoopItem loopItem = (LoopItem)get(componentName);
				if (loopItem == null)
				{
					// Create loopItem for index i of the list
					loopItem = newItem(i);

				    onBeginPopulateItem(loopItem);
					populateItem(loopItem);

					// Add item to loop
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
	 * Create a new LoopItem for loop item at index.
	 * 
	 * @param index
	 * @return LoopItem
	 */
	protected LoopItem newItem(final int index)
	{
		return new LoopItem(index);
	}

	/**
	 * Populate a given loopItem.
	 * 
	 * @param loopItem
	 *            The listItem to populate
	 */
	protected abstract void populateItem(final LoopItem loopItem);

	/**
	 * Comes handy for ready made Loop based components which must implement
	 * populateItem() but you don't want to loose compile time error checking 
	 * reminding the user to implement abstract populateItem().
	 * 
	 * @param loopItem
	 */
	protected void onBeginPopulateItem(final LoopItem loopItem)
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