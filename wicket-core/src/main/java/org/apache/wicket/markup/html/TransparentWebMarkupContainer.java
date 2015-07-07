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
package org.apache.wicket.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.resolver.ComponentResolvers;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

/**
 * A simple "transparent" markup container.
 * 
 * @author Juergen Donnerstag
 */
public class TransparentWebMarkupContainer extends WebMarkupContainer implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public TransparentWebMarkupContainer(String id)
	{
		super(id);
	}

	/**
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
	 *      org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		Component resolvedComponent = getParent().get(tag.getId());
		if (resolvedComponent != null && getPage().wasRendered(resolvedComponent))
		{
			/*
			 * Means that parent container has an associated homonymous tag to this grandchildren
			 * tag in markup. The parent container wants render it and it should be not resolved to
			 * their grandchildren.
			 */
			return null;
		}
		return resolvedComponent;
	}
	
	@Override
	public void internalRenderHead(HtmlHeaderContainer container)
	{
		if(isAjaxRequest())
		{
			renderHeadForInnerSiblings(container);
		}
		
		super.internalRenderHead(container);
	}

	private boolean isAjaxRequest()
	{
		Request request = RequestCycle.get().getRequest();
		
		if( request instanceof WebRequest)
		{	
			WebRequest webRequest = (WebRequest)request;
			return webRequest.isAjax();
		}
		
		return false;
	}

	private void renderHeadForInnerSiblings(HtmlHeaderContainer container)
	{
		MarkupStream stream = new MarkupStream(getMarkup());
		
		while (stream.hasMore())
		{
			MarkupElement childOpenTag = stream.nextOpenTag();
			
			if ((childOpenTag instanceof ComponentTag) && !stream.atCloseTag())
			{
				// Get element as tag
				final ComponentTag tag = (ComponentTag)childOpenTag;

				// Get component id
				final String id = tag.getId();

				Component component = null;
				
				if (get(id) == null)
				{
					component = ComponentResolvers.resolveByComponentHierarchy(this, stream, tag);					
				}
				
				if (component != null)
				{
					component.internalRenderHead(container);
				}		
				
				stream.skipToMatchingCloseTag(tag);
			}			
		}
	}
}