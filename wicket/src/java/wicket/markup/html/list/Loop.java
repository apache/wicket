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
 * abstract method populate(MarkupContainer container, int iteration). The
 * populate() method is called just before the container is rendered.
 * 
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class Loop extends WebMarkupContainer
{
	/**
	 * Container for a Loop iteration.
	 * 
	 * @author Jonathan Locke
	 */
	static public class Iteration extends WebMarkupContainer
	{
		/** The iteration number */
		private final int iteration;

		/**
		 * Constructor
		 * 
		 * @param iteration
		 *            The iteration of the loop
		 */
		private Iteration(final int iteration)
		{
			super(Integer.toString(iteration));
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
	 * @param id
	 *            See Component
	 * @param iterations
	 *            max index of the loop
	 * @see wicket.Component#Component(String, IModel)
	 */
	public Loop(final String id, final int iterations)
	{
		super(id, new Model(new Integer(iterations)));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            Must contain a Integer model object
	 * @see wicket.Component#Component(String, IModel)
	 */
	public Loop(final String id, final IModel model)
	{
		super(id, model);
	}

	/**
	 * @return The number of loop iterations
	 */
	public final int getIterations()
	{
		return ((Integer)getModelObject()).intValue();
	}

	/**
	 * @see wicket.Component#internalOnBeginRequest()
	 */
	protected void internalOnBeginRequest()
	{
		// Remove any previous loop contents
		removeAll();

		// Get number of iterations
		final int iterations = getIterations();
		if (iterations > 0)
		{
			// Loop through the markup in this container for each iteration
			for (int i = 0; i < iterations; i++)
			{
				// Create container for the given loop iteration
				final Iteration iteration = new Iteration(i);

				// Add iteration and populate it
				add(iteration);
				populateIteration(iteration);
			}
		}
	}

	/**
	 * Renders this Loop container.
	 */
	protected final void onRender()
	{
		// Ask parents for markup stream to use
		final MarkupStream markupStream = findMarkupStream();

		// Save position in markup stream
		final int markupStart = markupStream.getCurrentIndex();

		// Get number of iterations
		final int iterations = getIterations();
		if (iterations > 0)
		{
			// Loop through the markup in this container for each iteration
			for (int i = 0; i < iterations; i++)
			{
				// Create container for the given loop iteration
				final Iteration iteration = (Iteration)get(Integer.toString(i));
				if (iteration == null)
				{
					throw new WicketRuntimeException(
							"Loop iteration is null.  Probably the loop iterations were changed between onBeginRequest and render time.");
				}

				// Rewind to start of markup for kids
				markupStream.setCurrentIndex(markupStart);

				// Render iteration
				renderIteration(iteration);
			}
		}
		else
		{
			markupStream.skipComponent();
		}
	}

	/**
	 * Populates this loop iteration.
	 * 
	 * @param iteration
	 *            The iteration of the loop
	 */
	protected abstract void populateIteration(Iteration iteration);

	/**
	 * Renders this loop iteration.
	 * 
	 * @param iteration
	 *            The loop iteration
	 */
	protected void renderIteration(final Iteration iteration)
	{
		iteration.render();
	}
}
