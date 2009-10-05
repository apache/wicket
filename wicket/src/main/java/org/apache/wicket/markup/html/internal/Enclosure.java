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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.parser.filter.EnclosureHandler;
import org.apache.wicket.markup.resolver.ComponentResolvers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An Enclosure are automatically created by Wicket. Do not create it yourself. An Enclosure
 * container is created if &lt;wicket:enclosure&gt; is found in the markup. It is meant to solve the
 * following situation. Instead of
 * 
 * <pre>
 *    &lt;table wicket:id=&quot;label-container&quot; class=&quot;notify&quot;&gt;&lt;tr&gt;&lt;td&gt;&lt;span wicket:id=&quot;label&quot;&gt;[[notification]]&lt;/span&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;
 *    WebMarkupContainer container=new WebMarkupContainer(&quot;label-container&quot;)
 *    {
 *       public boolean isVisible()
 *       {
 *           return hasNotification();
 *       }
 *    };
 *    add(container);
 *     container.add(new Label(&quot;label&quot;, notificationModel));
 * </pre>
 * 
 * with Enclosure you are able to do the following:
 * 
 * <pre>
 *    &lt;wicket:enclosure&gt;
 *      &lt;table class=&quot;notify&quot;&gt;&lt;tr&gt;&lt;td&gt;&lt;span wicket:id=&quot;label&quot;&gt;[[notification]]&lt;/span&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;
 *    &lt;/wicket:enclosure&gt;
 *    add(new Label(&quot;label&quot;, notificationModel))
 *    {
 *       public boolean isVisible()
 *       {
 *           return hasNotification();
 *       }
 *    }
 * </pre>
 * 
 * @see EnclosureResolver
 * @see EnclosureHandler
 * 
 * @author Juergen Donnerstag
 * @since 1.3
 */
public class Enclosure extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(Enclosure.class);

	/** The child component to delegate the isVisible() call to */
	private Component childComponent;

	/** Id of the child component that will control visibility of the enclosure */
	private final CharSequence childId;

	private transient Map<Component, Boolean> changes;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param childId
	 */
	public Enclosure(final String id, final CharSequence childId)
	{
		super(id);

		if (childId == null)
		{
			throw new MarkupException(
				"You most likely forgot to register the EnclosureHandler with the MarkupParserFactory");
		}

		this.childId = childId;
	}

	/**
	 * 
	 * @see org.apache.wicket.MarkupContainer#isTransparentResolver()
	 */
	@Override
	public boolean isTransparentResolver()
	{
		return true;
	}

	/**
	 * Get the real parent container
	 * 
	 * @return enclosure's parent markup container
	 */
	private MarkupContainer getEnclosureParent()
	{
		MarkupContainer parent = getParent();
		while (parent != null)
		{
			if (parent.isTransparentResolver())
			{
				parent = parent.getParent();
			}
			else
			{
				break;
			}
		}

		if (parent == null)
		{
			throw new WicketRuntimeException(
				"Unable to find parent component which is not a transparent resolver");
		}
		return parent;
	}

	/**
	 * 
	 * @see org.apache.wicket.MarkupContainer#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, ComponentTag enclosureOpenTag)
	{
		// init changes map
		changes = new HashMap<Component, Boolean>();

		// enclosure's parent container
		final MarkupContainer container = getEnclosureParent();

		// iterate over all child tags and make sure all components are present, resolving them if
		// necessary
		ensureAllChildrenPresent(container, markupStream, enclosureOpenTag);

		Component controller = container.get(childId.toString());
		checkChildComponent(controller);

		// set the enclosure visibility
		setVisible(controller.determineVisibility());

		// transfer visibility to direct children
		applyEnclosureVisibilityToChildren(container, markupStream, enclosureOpenTag);

		// render components inside the enclosure if its visible or skip it if it is not
		if (isVisible() == true)
		{
			super.onComponentTagBody(markupStream, enclosureOpenTag);
		}
		else
		{
			markupStream.skipToMatchingCloseTag(enclosureOpenTag);
		}
	}

	/**
	 * 
	 * @param container
	 * @param markupStream
	 * @param enclosureOpenTag
	 */
	private void applyEnclosureVisibilityToChildren(final MarkupContainer container,
		final MarkupStream markupStream, final ComponentTag enclosureOpenTag)
	{
		DirectChildTagIterator it = new DirectChildTagIterator(markupStream, enclosureOpenTag);
		while (it.hasNext())
		{
			final ComponentTag tag = it.next();
			if (tag.isAutoComponentTag() == false)
			{
				final Component child = container.get(tag.getId());

				// record original visiblity allowed value, will restore later
				changes.put(child, child.isVisibilityAllowed());
				child.setVisibilityAllowed(isVisible());
			}
		}
		it.rewind();
	}

	/**
	 * 
	 * @param controller
	 */
	private void checkChildComponent(final Component controller)
	{
		if (controller == null)
		{
			throw new WicketRuntimeException("Could not find child with id: " + childId +
				" in the wicket:enclosure");
		}
		else if (controller == this)
		{
			throw new WicketRuntimeException(
				"Programming error: childComponent == enclose component; endless loop");
		}
	}

	/**
	 * 
	 * @param container
	 * @param markupStream
	 * @param enclosureOpenTag
	 */
	private void ensureAllChildrenPresent(final MarkupContainer container,
		final MarkupStream markupStream, ComponentTag enclosureOpenTag)
	{
		DirectChildTagIterator it = new DirectChildTagIterator(markupStream, enclosureOpenTag);
		while (it.hasNext())
		{
			final ComponentTag tag = it.next();

			if (tag.isAutoComponentTag() == false)
			{
				Component child = container.get(tag.getId());
				if (child == null)
				{
					// component does not yet exist in the container, attempt to resolve it using
					// resolvers
					final int tagIndex = it.getCurrentIndex();

					// because the resolvers can auto-add and therefore immediately render the
					// component
					// we have to buffer the output since we do not yet know the visibility of the
					// enclosure
					CharSequence buffer = new ResponseBufferZone(getRequestCycle(), markupStream)
					{
						@Override
						protected void executeInsideBufferedZone()
						{
							markupStream.setCurrentIndex(tagIndex);
							ComponentResolvers.resolve(container, markupStream, tag);
						}
					}.execute();

					child = container.get(tag.getId());
					checkChildComponent(child);

					if (buffer.length() > 0)
					{
						// we have already rendered this child component, insert a stub component
						// that
						// will dump the markup during the normal render process if the enclosure is
						// visible
						final Component stub = new AutoMarkupLabel(child.getId(), buffer);
						container.replace(stub); // ok here because we are replacing auto with auto
					}
				}
			}
		}
		it.rewind();
	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		restoreOriginalChildVisibility();
		super.onDetach();
	}

	/**
	 * 
	 */
	private void restoreOriginalChildVisibility()
	{
		if (changes != null)
		{
			MarkupContainer container = getEnclosureParent();

			// restore original visibility statuses
			for (Map.Entry<Component, Boolean> entry : changes.entrySet())
			{
				entry.getKey().setVisibilityAllowed(entry.getValue());
			}
			changes = null;
		}
	}
}
