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
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.resolver.ComponentResolvers;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

/**
 * A container whose markup can hold components that were added to its parent instead of to the
 * container itself. When a tag in the container's markup has a {@code wicket:id} that is not one of
 * the container's own children, the component is looked up in the container's parent.
 * <p>
 * Use it when a tag needs to be a component, for example to change its attributes, while the
 * components inside the tag stay where they are in the component hierarchy. A common case is a CSS
 * class on the {@code <body>} tag of a page:
 * 
 * <pre>{@code
 * <body wicket:id="body">
 *     <h1 wicket:id="title"></h1>
 * </body>
 * }</pre>
 * 
 * <pre>{@code
 * add(new TransparentWebMarkupContainer("body").add(AttributeModifier.append("class", "dark")));
 * add(new Label("title", "Hello"));
 * }</pre>
 * 
 * With a {@link WebMarkupContainer} in its place, rendering fails because the container has no
 * child {@code "title"}.
 * <p>
 * Things to know:
 * <ul>
 * <li>A component rendered this way is still a child of the parent. {@code get("title")} on the
 * container returns {@code null}, and the component's path does not contain the container's id.</li>
 * <li>A child added to the container itself is used before a component of the parent with the same
 * id.</li>
 * <li>Hiding the container hides everything in its markup, including the parent's components.</li>
 * <li>In an Ajax request, a component inside the container's markup can be added to the
 * {@code AjaxRequestTarget} on its own. Adding the container re-renders the components in its
 * markup, with their header contributions.</li>
 * <li>With markup inheritance, a base page can put the container around {@code <wicket:child/>}.
 * Components that a subclass adds to the page, with their markup in {@code <wicket:extend>},
 * render inside the container.</li>
 * <li>The lookup goes to the container's parent only. Inside the body of a
 * {@link org.apache.wicket.markup.html.border.Border}, the parent is the border, so the components
 * must be added to the border, not to the page.</li>
 * </ul>
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
	 *          The component id
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
		if (tag instanceof WicketTag && ((WicketTag)tag).isFragmentTag())
		{
			// even having a wicket:id it isn't a component's markup
			return null;
		}

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
		/*
		 * https://issues.apache.org/jira/browse/WICKET-5941
		 * 
		 * if this component is updated via AJAX and is the root
		 * one, then render head also for embedded components.
		 */
		if (isAjaxRequest() && !isParentRendering())
		{
			renderHeadForInnerSiblings(container);
		}

		super.internalRenderHead(container);
	}

	/**
	 * Says if parent container is rendering or not.
	 * 
	 * @return true if parent is rendering, false otherwise.
	 */
	private boolean isParentRendering()
	{
		MarkupContainer parent = getParent();

		return parent != null && parent.isRendering();
	}

	/**
	 * Says if the current request is an AJAX one.
	 * 
	 * @return true if the current request is an AJAX one, false otherwise.
	 */
	private boolean isAjaxRequest()
	{
		Request request = RequestCycle.get().getRequest();

		if (request instanceof WebRequest)
		{
			WebRequest webRequest = (WebRequest)request;
			return webRequest.isAjax();
		}

		return false;
	}

	/**
	 * Renders head for embedded component, i.e. those who are not added 
	 * directly to this container but have the markup inside it.
	 * 
	 * @param container
	 * 				The HtmlHeaderContainer
	 */
	private void renderHeadForInnerSiblings(HtmlHeaderContainer container)
	{
		MarkupStream stream = new MarkupStream(getMarkup());

		while (stream.isCurrentIndexInsideTheStream())
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

				//consider just direct children
				stream.skipToMatchingCloseTag(tag);
			}
		}
	}
	
	@Override
	protected Component findChildComponent(ComponentTag tag)
	{
		Component childComponent = super.findChildComponent(tag);
		
		return childComponent != null ? childComponent : resolve(this, null, tag);
	}
}
