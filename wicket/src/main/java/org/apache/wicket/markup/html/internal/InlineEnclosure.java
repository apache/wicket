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
package org.apache.wicket.markup.html.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.parser.filter.InlineEnclosureHandler;
import org.apache.wicket.markup.resolver.ComponentResolvers;
import org.apache.wicket.markup.resolver.EnclosureResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An InlineEnclosure are automatically created by Wicket. Do not create it yourself. An
 * InlineEnclosure container is created when &lt;tr wicket:enclosure="controllingChildId"&gt; (any
 * html tag which can contain other html tags can be used in place of &lt;tr&gt;) is found in the
 * markup. The child component (it's id defined as the value of the attribute, in the example,
 * 'controllingChildId') controls the visibility of the whole enclosure and it's children. This also
 * works in Ajax calls without extra markup or java code.
 * 
 * @see EnclosureResolver
 * @see InlineEnclosureHandler
 * 
 * @author Joonas Hamalainen
 */
public class InlineEnclosure extends Enclosure
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(InlineEnclosure.class);

	/** Id of the child component that will control visibility of the enclosure */
	private final CharSequence childId;

	/**
	 * This transient child list is filled only at onComponentTagBody. Therefore it is not
	 * necessarily accurate at any given time.
	 **/
	private transient List<Component> children = null;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param childId
	 * @param isInlineEnclosure
	 */
	public InlineEnclosure(final String id, final String childId)
	{
		super(id, childId);

		this.childId = childId;
	}

	@Override
	protected void onAfterRenderChildren()
	{
		if (children == null)
		{
			log.warn("Child list not yet generated.");
		}
		else
		{
			for (Component child : children)
			{
				child.afterRender();
			}
		}

		super.onAfterRenderChildren();
	}

	/**
	 * Return the controlling child component
	 * 
	 * @return the child component.
	 */
	private Component getChildComponent()
	{
		// enclosure's parent container
		final MarkupContainer container = getEnclosureParent();
		Component child = container.get(childId.toString());
		return child;
	}

	/**
	 * Update the visibility of this In-line enclosure with that of the controlling child.
	 * 
	 * @return the new visibility setting.
	 */
	public boolean updateVisibility()
	{
		Component child = getChildComponent();
		boolean visible = child.isVisible();
		setVisible(visible);
		return visible;
	}

	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, ComponentTag enclosureOpenTag)
	{
		enumerateChildren(markupStream, enclosureOpenTag);

		super.onComponentTagBody(markupStream, enclosureOpenTag);
	}

	private void enumerateChildren(final MarkupStream markupStream, ComponentTag enclosureOpenTag)
	{
		final MarkupContainer container = getEnclosureParent();
		// update child list
		children = new ArrayList<Component>();

		DirectChildTagIterator it = new DirectChildTagIterator(markupStream, enclosureOpenTag);
		while (it.hasNext())
		{
			final ComponentTag tag = it.next();
			if (tag.isAutoComponentTag() == false)
			{
				Component child = container.get(tag.getId());
				if (child == null)
				{
					// because the resolvers can auto-add and therefore immediately render the
					// component we have to buffer the output since we do not yet know the
					// visibility of the enclosure
					CharSequence buffer = new ResponseBufferZone(getRequestCycle(), markupStream)
					{
						@Override
						protected void executeInsideBufferedZone()
						{
							final int ind = markupStream.findComponentIndex(tag.getPath(),
								tag.getId());
							markupStream.setCurrentIndex(ind);
							ComponentResolvers.resolve(getApplication(), container, markupStream,
								tag);
						}
					}.execute();
					child = container.get(tag.getId());
				}
				children.add(child);
			}
		}
		it.rewind();
	}

	/**
	 * Returns the controlling child's Id
	 * 
	 * @return the id of the child component
	 */
	public String getChildId()
	{
		return childId.toString();
	}
}
