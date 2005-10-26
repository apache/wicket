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
package wicket.extensions.markup.html.repeater;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.model.IModel;

/**
 * A repeater view that renders all of its children, using its body markup, in
 * the order they were added.
 * 
 * @see wicket.extensions.markup.html.repeater.RepeatingView
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * 
 */
public class OrderedRepeatingView extends RepeatingView
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** List of ids of children components in the order they were added */
	private List order = new ArrayList();

	/** @see Component#Component(String) */
	public OrderedRepeatingView(String id)
	{
		super(id);
	}

	/** @see Component#Component(String, IModel) */
	public OrderedRepeatingView(String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * @see RepeatingView#renderIterator()
	 * 
	 * @return the iterator that iterators over children in the order they were
	 *         added
	 */
	protected final Iterator renderIterator()
	{
		final Iterator ids = order.iterator();
		return new Iterator()
		{
			public boolean hasNext()
			{
				return ids.hasNext();
			}

			public Object next()
			{
				return get((String)ids.next());
			}

			public void remove()
			{
				throw new UnsupportedOperationException();
			}

		};
	}

	/**
	 * @see MarkupContainer#add(Component)
	 */
	public MarkupContainer add(Component child)
	{
		super.add(child);
		order.add(child.getId());
		return this;
	}

	/**
	 * @see MarkupContainer#remove(String)
	 */
	public void remove(String id)
	{
		super.remove(id);
		order.remove(id);
	}

	/**
	 * @see MarkupContainer#removeAll()
	 */
	public void removeAll()
	{
		super.removeAll();
		order.clear();
	}
}