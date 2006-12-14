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
package wicket.markup.repeater;

import java.util.Iterator;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.MarkupStream;
import wicket.markup.html.list.AbstractRepeater;
import wicket.model.IModel;

/**
 * <p>
 * A repeater view that renders all of its children, using its body markup, in
 * order they were added.
 * 
 * </p>
 * Example:
 * <p>
 * <u>Java:</u>
 * 
 * <pre>
 * RepeatingView view = new RepeatingView(&quot;repeater&quot;);
 * view.add(new Label(&quot;1&quot;, &quot;hello&quot;));
 * view.add(new Label(&quot;2&quot;, &quot;goodbye&quot;));
 * view.add(new Label(&quot;3&quot;, &quot;good morning&quot;));
 * </pre>
 * 
 * </p>
 * <p>
 * <u>Markup:</u>
 * 
 * <pre>
 *     &lt;ul&gt;&lt;li wicket:id=&quot;repeater&quot;&gt;&lt;/li&gt;&lt;/ul&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * <u>Yields:</u>
 * 
 * <pre>
 *     &lt;ul&gt;&lt;li&gt;goodbye&lt;/li&gt;&lt;li&gt;hello&lt;/li&gt;&lt;li&gt;good morning&lt;/li&gt;&lt;/ul&gt;
 * </pre>
 * 
 * To expand a bit: the repeater itself produces no markup, instead every direct
 * child inherits the entire markup of the repeater. In the example above
 * reeaters's markup is:
 * 
 * <pre>
 *      &lt;li wicket:id=&quot;repeater&quot;&gt;&lt;/li&gt;
 * </pre>
 * 
 * and so this is the markup that is available to the direct children - the
 * Label components. So as each label renders it produces a line of the output
 * that has the <code>li</code>tag.
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public class RepeatingView<T> extends AbstractRepeater<T>
{
	private static final long serialVersionUID = 1L;

	/** Counter used for generating unique child component ids. */
	private long childIdCounter = 0;

	/** @see Component#Component(MarkupContainer,String) */
	public RepeatingView(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/** @see Component#Component(MarkupContainer,String, IModel) */
	public RepeatingView(MarkupContainer parent, final String id, IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * Generates a unique id string. This makes it easy to add items to be
	 * rendered w/out having to worry about generating unique id strings in your
	 * code.
	 * 
	 * @return unique child id
	 */
	public String newChildId()
	{
		childIdCounter++;

		if (childIdCounter == Long.MAX_VALUE)
		{
			// mmm yeah...like this will ever happen
			throw new RuntimeException("generateChildId() out of space.");
		}

		return String.valueOf(childIdCounter);
	}

	/**
	 * Renders all child items in no specified order
	 * 
	 * @param markupStream
	 *            The markup stream
	 */
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		final int markupStart = markupStream.getCurrentIndex();

		Iterator it = renderIterator();
		if (it.hasNext())
		{
			do
			{
				markupStream.setCurrentIndex(markupStart);
				renderChild((Component)it.next());
			}
			while (it.hasNext());
		}
		else
		{
			markupStream.skipComponent();
		}
	}

	/**
	 * Returns an iterator for the collection of child components to be
	 * rendered.
	 * 
	 * Child component are rendered in the order they are in the iterator. Since
	 * we use the iterator returned by wicket's
	 * <code>MarkupContainer#iterator()</code> method and that method does not
	 * guarantee any kind of ordering neither do we. This method can be
	 * overridden by subclasses to create an ordering scheme, see
	 * <code>OrderedRepeatingView#renderIterator()</code>.
	 * 
	 * @return iterator over child components to be rendered
	 */
	protected Iterator renderIterator()
	{
		return iterator();
	}

	/**
	 * Render a single child. This method can be overridden to modify how a
	 * single child component is rendered.
	 * 
	 * @param child
	 *            Child component to be rendered
	 */
	protected void renderChild(final Component child)
	{
		child.render(getMarkupStream());
	}


}
