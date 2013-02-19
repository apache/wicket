/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.list;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.collections.ReadOnlyIterator;

/**
 * A very simple loop component whose model is an Integer defining the number of iterations the loop
 * should render. During rendering, Loop iterates from 0 to getIterations() - 1, creating a new
 * MarkupContainer for each iteration. The MarkupContainer is populated by the Loop subclass by
 * implementing the abstract method populate(LoopItem). The populate() method is called just before
 * the LoopItem container is rendered.
 * 
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class Loop extends AbstractRepeater
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            See Component
	 * @param iterations
	 *            max index of the loop
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Loop(final String id, final int iterations)
	{
		super(id, new Model<Integer>(iterations));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            Must contain a Integer model object
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Loop(final String id, final IModel<Integer> model)
	{
		super(id, model);
	}

	/**
	 * @return The number of loop iterations
	 */
	public final int getIterations()
	{
		return (Integer)getDefaultModelObject();
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected final void onPopulate()
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
				add(item);
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
		return new LoopItem(iteration);
	}

	/**
	 * @see org.apache.wicket.markup.repeater.AbstractRepeater#renderIterator()
	 */
	@Override
	protected Iterator<Component> renderIterator()
	{
		final int iterations = size();

		return new ReadOnlyIterator<Component>()
		{
			private int index = 0;

			@Override
			public boolean hasNext()
			{
				return index < iterations;
			}

			@Override
			public Component next()
			{
				return get(Integer.toString(index++));
			}
		};
	}

	/**
	 * Populates this loop item.
	 * 
	 * @param item
	 *            The iteration of the loop
	 */
	protected abstract void populateItem(LoopItem item);

	/**
	 * @param child
	 */
	@Override
	protected final void renderChild(Component child)
	{
		renderItem((LoopItem)child);
	}

	/**
	 * Renders this loop iteration.
	 * 
	 * @param item
	 *            The loop iteration
	 */
	protected void renderItem(final LoopItem item)
	{
		item.render();
	}
}
