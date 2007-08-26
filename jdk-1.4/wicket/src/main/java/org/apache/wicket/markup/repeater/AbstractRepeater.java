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
package org.apache.wicket.markup.repeater;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for repeaters. This container renders each of its children using its own markup.
 * 
 * The children are collected using {@link #renderIterator()} method. This class will take care of
 * properly positioning and rewinding its markup stream so before each child renders it points to
 * the beginning of this component. Each child is rendered by a call to
 * {@link #renderChild(Component)}. A typical implementation simply does
 * <code>child.render(getMarkupStream());</code>.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractRepeater extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(AbstractRepeater.class);

	/**
	 * Constructor
	 * 
	 * @param id
	 */
	public AbstractRepeater(String id)
	{
		super(id);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 */
	public AbstractRepeater(String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * Returns an iterator for the collection of child components to be rendered. Users can override
	 * this to change order of rendered children.
	 * 
	 * @return iterator over child components to be rendered
	 */
	protected abstract Iterator renderIterator();

	/**
	 * Renders all child items in no specified order
	 * 
	 * @param markupStream
	 *            The markup stream
	 */
	protected final void onRender(final MarkupStream markupStream)
	{
		final int markupStart = markupStream.getCurrentIndex();

		Iterator it = renderIterator();
		if (it.hasNext())
		{
			do
			{
				Component child = (Component)it.next();
				if (child == null)
				{
					throw new IllegalStateException("the render iterator returned null for a child");
				}
				markupStream.setCurrentIndex(markupStart);
				renderChild(child);
			}
			while (it.hasNext());
		}
		else
		{
			markupStream.skipComponent();
		}
	}

	/**
	 * Render a single child. This method can be overridden to modify how a single child component
	 * is rendered.
	 * 
	 * @param child
	 *            Child component to be rendered
	 */
	protected void renderChild(final Component child)
	{
		child.render(getMarkupStream());
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	protected final void onBeforeRender()
	{
		if (isVisibleInHierarchy())
		{
			onPopulate();
		}
		super.onBeforeRender();
	}

	/**
	 * Callback to let the repeater know it should populate itself with its items.
	 */
	protected abstract void onPopulate();

}
