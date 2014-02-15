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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.DequeueContext;
import org.apache.wicket.DequeueContext.Bookmark;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupFragment;
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
 * <code>child.render();</code>.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractRepeater extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(AbstractRepeater.class);

	private static Pattern SAFE_CHILD_ID_PATTERN = Pattern.compile("^\\d+$");

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
	public AbstractRepeater(String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * Returns an iterator for the collection of child components to be rendered. Users can override
	 * this to change order of rendered children.
	 * 
	 * @return iterator over child components to be rendered
	 */
	protected abstract Iterator<? extends Component> renderIterator();

	/**
	 * Renders all child items in no specified order
	 */
	@Override
	protected final void onRender()
	{
		Iterator<? extends Component> it = renderIterator();
		while (it.hasNext())
		{
			Component child = it.next();
			if (child == null)
			{
				throw new IllegalStateException(
					"The render iterator returned null for a child. Container: " + this.toString() +
						"; Iterator=" + it.toString());
			}
			renderChild(child);
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
		child.render();
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		onPopulate();

		if (getApplication().usesDevelopmentConfig())
		{
			Iterator<? extends Component> i = iterator();
			while (i.hasNext())
			{
				Component c = i.next();
				Matcher matcher = SAFE_CHILD_ID_PATTERN.matcher(c.getId());
				if (!matcher.matches())
				{
					log.warn("Child component of repeater " + getClass().getName() + ":" + getId() +
						" has a non-safe child id of " + c.getId() +
						". Safe child ids must be composed of digits only.");
					// do not flood the log
					break;
				}

			}
		}
		super.onBeforeRender();
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#getMarkup(org.apache.wicket.Component)
	 */
	@Override
	public IMarkupFragment getMarkup(final Component child)
	{
		// each direct child gets the markup of this repeater
		return getMarkup();
	}

	/**
	 * Callback to let the repeater know it should populate itself with its items.
	 */
	protected abstract void onPopulate();
	
	@Override
	public void dequeue(DequeueContext dequeue) {
		if (size()>0) {
			Bookmark bookmark=dequeue.save();
			
			for (Component child:this) {
				if (child instanceof MarkupContainer) {
					dequeue.popContainer(); // pop the repeater
					MarkupContainer container=(MarkupContainer) child;
					dequeue.pushContainer(container);
					container.dequeue(dequeue);
					dequeue.restore(bookmark);
				}
			}	
		}
		dequeue.skipToCloseTag();
	}
}
