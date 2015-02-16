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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.EnclosureContainer;
import org.apache.wicket.markup.parser.filter.EnclosureHandler;
import org.apache.wicket.markup.resolver.ComponentResolvers;
import org.apache.wicket.markup.resolver.ComponentResolvers.ResolverFilter;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.util.string.Strings;
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
 * <p>
 * Please note that since a transparent auto-component is created for the tag, the markup and the
 * component hierarchy will not be in sync which leads to subtle differences if your code relies on
 * onBeforeRender() and validate() being called for the children inside the enclosure tag. E.g. it
 * might happen that onBeforeRender() and validate() gets called on invisible components. In doubt,
 * please fall back to {@link EnclosureContainer}.
 * </p>
 * <p>
 * Additionally due to the reason above it is not possible to assert that children in Enclosure are
 * not visible to WicketTester.
 * </p>
 * 
 * @see EnclosureHandler
 * @see EnclosureContainer
 * 
 * @author Juergen Donnerstag
 * @since 1.3
 */
public class Enclosure extends WebMarkupContainer implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(Enclosure.class);

	/** The child component to delegate the isVisible() call to */
	private Component childComponent;

	/** Id of the child component that will control visibility of the enclosure */
	private final CharSequence childId;

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
	 * @return child id
	 */
	public final String getChildId()
	{
		return childId.toString();
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		// get Child Component. If not "added", ask a resolver to find it.
		childComponent = getChildComponent(new MarkupStream(getMarkup()), getEnclosureParent());
	}

	protected final Component getChild()
	{
		return childComponent;
	}

	@Override
	public boolean isVisible()
	{
		return childComponent.determineVisibility() && super.isVisible();
	}
	
	@Override
	protected void onConfigure()
	{
		super.onConfigure();
		final Component child = getChild();
		
		child.configure();
		boolean childVisible = child.determineVisibility();
		
		setVisible(childVisible);
	}
	/**
	 * Get the real parent container
	 * 
	 * @return enclosure's parent markup container
	 */
	protected MarkupContainer getEnclosureParent()
	{
		MarkupContainer parent = getParent();
		while ((parent != null) && parent.isAuto())
		{
			parent = parent.getParent();
		}

		if (parent == null)
		{
			throw new WicketRuntimeException(
				"Unable to find parent component which is not a transparent resolver");
		}
		return parent;
	}

	/**
	 * Resolves the child component which is the controller of this Enclosure
	 * 
	 * @param markupStream
	 *            the markup stream of this Enclosure
	 * @param enclosureParent
	 *            the non-auto parent component of this Enclosure
	 * @return The component associated with the {@linkplain #childId}
	 */
	private Component getChildComponent(final MarkupStream markupStream,
		MarkupContainer enclosureParent)
	{
		String fullChildId = getChildId();

		Component controller = enclosureParent.get(fullChildId);
		if (controller == null)
		{
			int orgIndex = markupStream.getCurrentIndex();
			try
			{
				while (markupStream.hasMore())
				{
					markupStream.next();
					if (markupStream.skipUntil(ComponentTag.class))
					{
						ComponentTag tag = markupStream.getTag();
						if ((tag != null) && (tag.isOpen() || tag.isOpenClose()))
						{
							String tagId = tag.getId();

							if (fullChildId.equals(tagId))
							{
								ComponentTag fullComponentTag = new ComponentTag(tag);
								fullComponentTag.setId(childId.toString());

								controller = ComponentResolvers.resolve(enclosureParent,
									markupStream, fullComponentTag, new ResolverFilter()
									{
										@Override
										public boolean ignoreResolver(
											final IComponentResolver resolver)
										{
											return resolver instanceof EnclosureHandler;
										}
									});
								break;
							}
							else if (fullChildId.startsWith(tagId + PATH_SEPARATOR))
							{
								fullChildId = Strings.afterFirst(fullChildId, PATH_SEPARATOR);
							}
						}
					}
				}
			}
			finally
			{
				markupStream.setCurrentIndex(orgIndex);
			}
		}

		checkChildComponent(controller);
		return controller;
	}

	@Override
	public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		if (childId.equals(tag.getId()))
		{
			return childComponent;
		}
		return getEnclosureParent().get(tag.getId());
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
}
