/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 07:46:36 +0200 (vr, 26 mei 2006) $
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

import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * A very simple loop component whose model is an Integer defining the number of
 * iterations the loop should render. During rendering, Loop iterates from 0 to
 * getIterations() - 1, creating a new MarkupContainer for each iteration. The
 * MarkupContainer is populated by the Loop subclass by implementing the
 * abstract method populate(LoopItem). The populate() method is called just
 * before the LoopItem container is rendered.
 * 
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class Loop extends WebMarkupContainer<Integer>
{
	/**
	 * Item container for a Loop iteration.
	 * 
	 * @author Jonathan Locke
	 */
	public static final class LoopItem extends WebMarkupContainer<Integer>
	{
		private static final long serialVersionUID = 1L;

		/** The iteration number */
		private final int iteration;

		/**
		 * Constructor
		 * 
		 * @param parent
		 *            The parent of this component
		 * 
		 * @param iteration
		 *            The iteration of the loop
		 */
		private LoopItem(MarkupContainer parent, final int iteration)
		{
			super(parent, Integer.toString(iteration));
			this.iteration = iteration;
		}

		/**
		 * @return Returns the iteration.
		 */
		public int getIteration()
		{
			return iteration;
		}
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param iterations
	 *            max index of the loop
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public Loop(MarkupContainer parent, final String id, final int iterations)
	{
		super(parent, id, new Model<Integer>(iterations));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            Must contain a Integer model object
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public Loop(MarkupContainer parent, final String id, final IModel<Integer> model)
	{
		super(parent, id, model);
	}

	/**
	 * @return The number of loop iterations
	 */
	public final int getIterations()
	{
		return getModelObject();
	}

	/**
	 * @see wicket.Component#internalOnAttach()
	 */
	@Override
	protected void internalOnAttach()
	{
		// Remove any previous loop contents
		removeAll();

		// Get number of iterations
		final int iterations = getIterations();
		if (iterations > 0)
		{
			// Create LoopItems for each iteration
			for (int iteration = 0; iteration < iterations; iteration++)
			{
				// Create item for loop iteration
				LoopItem item = newItem(iteration);

				// Add and populate item
				populateItem(item);
			}
		}
	}

	/**
	 * Create a new LoopItem for loop at iteration.
	 * 
	 * @param iteration
	 *            iteration in the loop
	 * @return LoopItem
	 */
	protected LoopItem newItem(int iteration)
	{
		return new LoopItem(this, iteration);
	}

	/**
	 * 
	 * @see wicket.Component#onRender(wicket.markup.MarkupStream)
	 */
	@Override
	protected final void onRender(final MarkupStream markupStream)
	{
		// Save position in markup stream
		final int markupStart = markupStream.getCurrentIndex();

		// Get number of iterations
		final int iterations = getIterations();
		if (iterations > 0)
		{
			// Loop through the markup in this container for each item
			for (int iteration = 0; iteration < iterations; iteration++)
			{
				// Get item for iteration
				final LoopItem item = (LoopItem)get(Integer.toString(iteration));

				// Item should have been constructed in internalOnBeginRequest
				if (item == null)
				{
					throw new WicketRuntimeException(
							"Loop item is null.  Probably the number of loop iterations were changed between onBeginRequest and render time.");
				}

				// Rewind to start of markup for kids
				markupStream.setCurrentIndex(markupStart);

				// Render iteration
				renderItem(item);
			}
		}
		else
		{
			markupStream.skipComponent();
		}
	}

	/**
	 * Populates this loop item.
	 * 
	 * @param item
	 *            The iteration of the loop
	 */
	protected abstract void populateItem(LoopItem item);

	/**
	 * Renders this loop iteration.
	 * 
	 * @param item
	 *            The loop iteration
	 */
	protected void renderItem(final LoopItem item)
	{
		item.render(getMarkupStream());
	}
}
