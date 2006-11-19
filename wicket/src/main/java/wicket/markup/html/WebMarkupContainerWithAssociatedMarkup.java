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
package wicket.markup.html;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.markup.resolver.IComponentResolver;
import wicket.model.IModel;

/**
 * 
 * @author Juergen Donnerstag
 */
public class WebMarkupContainerWithAssociatedMarkup extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/** A utility class which implements the internals */
	private ContainerWithAssociatedMarkupHelper markupHelper;
	
	/**
	 * @see Component#Component(String)
	 */
	public WebMarkupContainerWithAssociatedMarkup(final String id)
	{
		super(id);
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public WebMarkupContainerWithAssociatedMarkup(final String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * Called by components like Panel and Border which have associated Markup
	 * and which may have a &lt;wicket:head&gt; tag.
	 * <p>
	 * Whereas 'this' might be a Panel or Border, the HtmlHeaderContainer
	 * parameter has been added to the Page as a container for all headers any
	 * of its components might wish to contribute.
	 * <p>
	 * The headers contributed are rendered in the standard way.
	 * 
	 * @param container
	 *            The HtmlHeaderContainer added to the Page
	 */
	protected final void renderHeadFromAssociatedMarkupFile(final HtmlHeaderContainer container)
	{
		if (markupHelper == null)
		{
			markupHelper = new ContainerWithAssociatedMarkupHelper(this);
		}

		markupHelper.renderHeadFromAssociatedMarkupFile(container);
	}
	
	/**
	 * Create a new HeaderPartContainer. Users may wish to do that to
	 * implemented more sophisticated header scoping stragegies. 
	 * 
	 * @param id The header component's id
	 * @param scope The default scope of the header
	 * @return The new HeaderPartContainer
	 */
	public HeaderPartContainer newHeaderPartContainer(final String id, final String scope)
	{
		return new HeaderPartContainer(id, this, scope);
	}

	/**
	 * For each wicket:head tag a HeaderPartContainer is created and added to
	 * the HtmlHeaderContainer which has been added to the Page.
	 */
	public static final class HeaderPartContainer extends WebMarkupContainer
			implements
				IComponentResolver
	{
		private static final long serialVersionUID = 1L;

		/** The panel or bordered page the header part is associated with */
		private final MarkupContainer container;

		/** <wicket:head scope="...">. A kind of namespace */
		private final String scope;

		/**
		 * @param id
		 *            The component id
		 * @param container
		 *            The Panel (or bordered page) the header part is associated
		 *            with
		 * @param scope
		 *            The scope of the wicket:head tag
		 */
		public HeaderPartContainer(final String id, final MarkupContainer container,
				final String scope)
		{
			super(id);
			this.container = container;
			this.scope = scope;
		}

		/**
		 * Get the scope of the header part
		 * 
		 * @return The scope name
		 */
		public final String getScope()
		{
			return this.scope;
		}

		/**
		 * @see IComponentResolver#resolve(MarkupContainer, MarkupStream,
		 *      ComponentTag)
		 */
		public final boolean resolve(final MarkupContainer container,
				final MarkupStream markupStream, final ComponentTag tag)
		{
			// The tag must be resolved against the panel and not against the
			// page
			Component component = this.container.get(tag.getId());
			if (component != null)
			{
				component.render(markupStream);
				return true;
			}

			return false;
		}

		/**
		 * @see #setMarkupStream(MarkupStream)
		 * 
		 * @param markupStream
		 */
		public final void setMyMarkupStream(final MarkupStream markupStream)
		{
			super.setMarkupStream(markupStream);
		}
	}
}