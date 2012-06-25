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
import org.apache.wicket.model.IModel;


/**
 * <p>
 * A repeater view that renders all of its children, using its body markup, in the order they were
 * added.
 * 
 * </p>
 * Example:
 * <p>
 * <u>Java:</u>
 * 
 * <pre>
 * RepeatingView view = new RepeatingView(&quot;repeater&quot;);
 * view.add(new Label(view.newChildId(), &quot;hello&quot;));
 * view.add(new Label(view.newChildId(), &quot;goodbye&quot;));
 * view.add(new Label(view.newChildId(), &quot;good morning&quot;));
 * add(view);
 * </pre>
 * 
 * </p>
 * <p>
 * <u>Markup:</u>
 * 
 * <pre>
 *  &lt;ul&gt;&lt;li wicket:id=&quot;repeater&quot;&gt;&lt;/li&gt;&lt;/ul&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * <u>Yields:</u>
 * 
 * <pre>
 *  &lt;ul&gt;&lt;li&gt;hello&lt;/li&gt;&lt;li&gt;goodbye&lt;/li&gt;&lt;li&gt;good morning&lt;/li&gt;&lt;/ul&gt;
 * </pre>
 * 
 * To expand a bit: the repeater itself produces no markup, instead every direct child inherits the
 * entire markup of the repeater. In the example above repeaters's markup is:
 * 
 * <pre>
 *  &lt;li wicket:id=&quot;repeater&quot;&gt;&lt;/li&gt;
 * </pre>
 * 
 * and so this is the markup that is available to the direct children - the Label components. So as
 * each label renders it produces a line of the output that has the <code>li</code>tag.
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public class RepeatingView extends AbstractRepeater
{
	private static final long serialVersionUID = 1L;

	/** Counter used for generating unique child component ids. */
	private long childIdCounter = 0;

	/** @see Component#Component(String) */
	public RepeatingView(String id)
	{
		super(id);
	}

	/** @see Component#Component(String, IModel) */
	public RepeatingView(String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * Generates a unique id string. This makes it easy to add items to be rendered w/out having to
	 * worry about generating unique id strings in your code.
	 * 
	 * @return unique child id
	 */
	public String newChildId()
	{
		childIdCounter++;
		return String.valueOf(childIdCounter).intern();
	}

	/**
	 * @see org.apache.wicket.markup.repeater.AbstractRepeater#renderIterator()
	 */
	@Override
	protected Iterator<? extends Component> renderIterator()
	{
		return iterator();
	}

	/**
	 * @see org.apache.wicket.markup.repeater.AbstractRepeater#onPopulate()
	 */
	@Override
	protected void onPopulate()
	{
		// noop - population of this repeater is manual
	}
}
