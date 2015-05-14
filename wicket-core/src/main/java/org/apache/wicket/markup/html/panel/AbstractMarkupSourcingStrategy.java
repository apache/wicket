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
package org.apache.wicket.markup.html.panel;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.util.lang.Classes;

/**
 * Implements boilerplate as needed by many markup sourcing strategies.
 * 
 * @author Juergen Donnerstag
 */
public abstract class AbstractMarkupSourcingStrategy implements IMarkupSourcingStrategy
{
	/**
	 * Construct.
	 */
	public AbstractMarkupSourcingStrategy()
	{
	}

	@Override
	public abstract IMarkupFragment getMarkup(final MarkupContainer container, final Component child);

	/**
	 * If the child has not been directly added to the container, but via a
	 * TransparentWebMarkupContainer, then we are in trouble. In general Wicket iterates over the
	 * markup elements and searches for associated components, not the other way around. Because of
	 * TransparentWebMarkupContainer (or more generally resolvers), there is no "synchronous" search
	 * possible.
	 * 
	 * @param container
	 *            the parent container.
	 * @param
	 * 		  containerMarkup
	 * 			  the markup of the container.           
	 * @param child
	 *            The component to find the markup for.
	 * @return the markup fragment for the child, or {@code null}.
	 */
	protected IMarkupFragment searchMarkupInTransparentResolvers(MarkupContainer container,
		IMarkupFragment containerMarkup, Component child)
	{
		IMarkupFragment childMarkupFound = null;
		Iterator<Component> siblingsIterator = container.iterator();
		
		while (siblingsIterator.hasNext() && childMarkupFound == null)
		{
			Component sibling = siblingsIterator.next();
			
			if(sibling == child || !sibling.isVisible())
			{
				continue;
			}
			
			IMarkupFragment siblingMarkup = containerMarkup.find(sibling.getId());
			
			if (siblingMarkup != null && sibling instanceof MarkupContainer)
			{
				IMarkupFragment childMarkup  = siblingMarkup.find(child.getId());
				
				if (childMarkup != null && sibling instanceof IComponentResolver)
				{
					IComponentResolver componentResolver = (IComponentResolver)sibling;
					MarkupStream stream = new MarkupStream(childMarkup);
					ComponentTag tag = stream.getTag();
					
					Component resolvedComponent = sibling.get(tag.getId());
					if (resolvedComponent == null)
					{
						resolvedComponent = componentResolver.resolve((MarkupContainer)sibling, stream, tag);
					}
					
					if (child == resolvedComponent)
					{
						childMarkupFound = childMarkup;
					}
				}
				else 
				{
					childMarkupFound = searchMarkupInTransparentResolvers((MarkupContainer)sibling, siblingMarkup, child);
				}
			}
		}
		return childMarkupFound;
	}

	/**
	 * Make sure we open up open-close tags to open-body-close
	 */
	@Override
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
		if (tag.isOpenClose())
		{
			tag.setType(TagType.OPEN);
		}
	}

	/**
	 * Skip the components body which is expected to be raw markup only (no wicket components). It
	 * will be replaced by the associated markup.
	 */
	@Override
	public void onComponentTagBody(final Component component, final MarkupStream markupStream,
		final ComponentTag openTag)
	{
		// Skip the components body. It will be replaced by the associated markup or fragment
		if (markupStream.getPreviousTag().isOpen())
		{
			markupStream.skipRawMarkup();
			if (markupStream.get().closes(openTag) == false)
			{
				throw new MarkupException(
					markupStream,
					"Close tag not found for tag: " +
						openTag.toString() +
						". For " +
							Classes.simpleName(component.getClass()) +
								" Components only raw markup is allow in between the tags but not other Wicket Component." +
								". Component: " + component.toString());
			}
		}
	}

	/**
	 * Empty. Nothing to be added to the response by default.
	 */
	@Override
	public void renderHead(final Component component, HtmlHeaderContainer container)
	{
	}
}
