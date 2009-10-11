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
package org.apache.wicket.markup.html.border;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupFragment;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.IModel;

/**
 * A border component has associated markup which is drawn and determines placement of markup and/or
 * components nested within the border component.
 * <p>
 * The portion of the border's associated markup file which is to be used in rendering the border is
 * denoted by a &lt;wicket:border&gt; tag. The children of the border component instance are then
 * inserted into this markup, replacing the first &lt;wicket:body&gt; tag in the border's associated
 * markup.
 * <p>
 * For example, if a border's associated markup looked like this:
 * 
 * <pre>
 *   &lt;html&gt;
 *   &lt;body&gt;
 *     &lt;wicket:border&gt;
 *       First &lt;wicket:body/&gt; Last
 *     &lt;/wicket:border&gt;
 *   &lt;/body&gt;
 *   &lt;/html&gt;
 * </pre>
 * 
 * And the border was used on a page like this:
 * 
 * <pre>
 *   &lt;html&gt;
 *   &lt;body&gt;
 *     &lt;span wicket:id = &quot;myBorder&quot;&gt;
 *       Middle
 *     &lt;/span&gt;
 *   &lt;/body&gt;
 *   &lt;/html&gt;
 * </pre>
 * 
 * Then the resulting HTML would look like this:
 * 
 * <pre>
 *   &lt;html&gt;
 *   &lt;body&gt;
 *     First Middle Last
 *   &lt;/body&gt;
 *   &lt;/html&gt;
 * </pre>
 * 
 * In other words, the body of the myBorder component is substituted into the border's associated
 * markup at the position indicated by the &lt;wicket:body&gt; tag.
 * <p>
 * Regarding &lt;wicket:body/&gt; you have two options. Either use &lt;wicket:body/&gt; (open-close
 * tag) which will automatically be expanded to &lt;wicket:body&gt;body content&lt;/wicket:body&gt;
 * or use &lt;wicket:body&gt;preview region&lt;/wicket:body&gt; in your border's markup. The preview
 * region (everything in between the open and close tag) will automatically be removed.
 * <p>
 * The border body container will automatically be created for you and added to the border
 * container. It is accessible via {@link #getBodyContainer()}. In case the body markup is not an
 * immediate child of border (see the example below), than you must use code such as
 * <code>someContainer.add(getBodyContainer())</code> to add the body component to the correct
 * container.
 * 
 * <pre>
 *   &lt;html&gt;
 *   &lt;body&gt;
 *     &lt;wicket:border&gt;
 *       &lt;span wicket:id=&quot;someContainer&quot;&gt;
 *         &lt;wicket:body/&gt;
 *       &lt;/span&gt;
 *     &lt;/wicket:border&gt;
 *   &lt;/body&gt;
 *   &lt;/html&gt;
 * </pre>
 * 
 * The components "someContainer" in the previous example must be added to the border, and not the
 * body, which is achieved via {@link #addToBorder(Component...)}.
 * <p/>
 * {@link #add(Component...)} or {@link #addToBorderBody(Component...)} will add a child component
 * to the border body as shown in the example below.
 * 
 * <pre>
 *   &lt;html&gt;
 *   &lt;body&gt;
 *     &lt;span wicket:id = &quot;myBorder&quot;&gt;
 *       &lt;input wicket:id=&quot;name&quot/;&gt;
 *     &lt;/span&gt;
 *   &lt;/body&gt;
 *   &lt;/html&gt;
 * </pre>
 * 
 * This implementation does not apply any magic with respect to component handling. In doubt think
 * simple. But everything you can do with a MarkupContainer or Component, you can do with a Border
 * or its Body as well.
 * <p/>
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public abstract class Border extends WebMarkupContainerWithAssociatedMarkup
	implements
		IComponentResolver
{
	private static final long serialVersionUID = 1L;

	static final String BODY = "body";
	static final String BORDER = "border";

	static
	{
		// register "wicket:body" and "wicket:border"
		WicketTagIdentifier.registerWellKnownTagName(BORDER);
		WicketTagIdentifier.registerWellKnownTagName(BODY);
	}

	/** Must be the same as the id automatically assigned to <wicket:body> */
	static final String BODY_ID = "_body";

	/** The body component associated with <wicket:body> */
	private final BorderBodyContainer body;

	/**
	 * only required during render phase. The markup stream associated with <span
	 * wicket:id="myBorder"
	 */
	private transient MarkupStream originalMarkupStream;

	/** only required during render phase. The <span wicket:id="myBorder"> tag */
	private transient ComponentTag openTag;

	/** */
	private int beginOfBodyIndex;

	/** true, if body is currently being rendered */
	private transient boolean rendering;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public Border(final String id)
	{
		this(id, (IModel<?>)null);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Border(final String id, final IModel<?> model)
	{
		super(id, model);

		body = newBorderBodyContainer(id + BODY_ID);
		addToBorder(body);
	}

	/**
	 * @return The border body container
	 */
	public final BorderBodyContainer getBodyContainer()
	{
		return body;
	}

	/**
	 * Create a new BorderBodyContainer
	 * 
	 * @param id
	 * @return A new instance
	 */
	BorderBodyContainer newBorderBodyContainer(final String id)
	{
		return new BorderBodyContainer(id);
	}

	/**
	 * This is for all components which have been added to the markup like this:
	 * 
	 * <pre>
	 *   &lt;span wicket:id="myBorder"&gt;
	 *     &lt;input wicket:id="text1" .. /&gt;
	 *     ...
	 *   &lt;/span&gt;
	 * </pre>
	 * 
	 * Whereas {@link #addToBorder(Component...)} will add a component associated with the following
	 * markup:
	 * 
	 * <pre>
	 *   &lt;wicket:border&gt;
	 *     &lt;form wicket:id="myForm" .. &gt;
	 *        &lt;body/&gt;
	 *     &lt;/form&gt;
	 *   &lt;/wicket:border&gt;
	 * </pre>
	 * 
	 * @param child
	 * @return this
	 */
	@Override
	public MarkupContainer add(final Component... children)
	{
		return addToBorderBody(children);
	}

	/**
	 * Add a behavior to the border's body.
	 * 
	 * @see #add(Component...)
	 */
	@Override
	public Component add(final IBehavior... behaviors)
	{
		return addToBorderBody(behaviors);
	}

	/**
	 * @see #add(Component...)
	 * @param children
	 * @return this
	 */
	public MarkupContainer addToBorder(final Component... children)
	{
		return super.add(children);
	}

	/**
	 * @see #add(Component...)
	 * @param behaviors
	 * @return this
	 */
	public Component addToBorder(final IBehavior... behaviors)
	{
		return super.add(behaviors);
	}

	/**
	 * @see #add(Component...)
	 * @param child
	 * @return this
	 */
	public Border addToBorderBody(final Component... child)
	{
		getBodyContainer().add(child);
		return this;
	}

	/**
	 * @see #add(Component...)
	 * @param behaviors
	 * @return this
	 */
	public Component addToBorderBody(final IBehavior... behaviors)
	{
		super.add(behaviors);
		return this;
	}

	/**
	 * @see #addToBorderBody(Component...)
	 * @set #get(String)
	 * @param path
	 * @return The component added to the border
	 */
	public final Component getFromBorderBody(final String path)
	{
		return getBodyContainer().get(path);
	}

	/**
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
	 *      org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		// make sure nested borders are resolved properly
		if (rendering == false)
		{
			// We are only interested in border body tags. The tag ID actually is irrelevant since
			// always preset with the same default
			if (tag instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag)tag;
				if (wtag.isBodyTag())
				{
					rendering = true;
					try
					{
						body.render(markupStream);
					}
					finally
					{
						rendering = false;
					}
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		if (tag.isOpen() == false)
		{
			throw new WicketRuntimeException(
				"The border tag must be an open tag. Open-close is not allowed: " + tag.toString());
		}

		super.onComponentTag(tag);
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected final void onComponentTagBody(final MarkupStream markupStream,
		final ComponentTag openTag)
	{
		// Remember the data for easy access by the Body component
		this.openTag = openTag;
		originalMarkupStream = markupStream;

		// Remember the current position (start of border-body) of the markupstream
		beginOfBodyIndex = originalMarkupStream.getCurrentIndex();

		// Render the associated markup
		renderAssociatedMarkup("border",
			"Markup for a border component must begin a tag like '<wicket:border>'");

		markupStream.skipToMatchingCloseTag(openTag);
	}

	/**
	 * @see org.apache.wicket.Component#renderHead(org.apache.wicket.markup.html.internal.HtmlHeaderContainer)
	 */
	@Override
	public void renderHead(HtmlHeaderContainer container)
	{
		renderHeadFromAssociatedMarkupFile(container);
		super.renderHead(container);
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#getMarkup(org.apache.wicket.Component)
	 */
	@Override
	public IMarkupFragment getMarkup(final Component child)
	{
		// Border require an associated markup resource file
		IMarkupFragment markup = getAssociatedMarkup();
		if (markup == null)
		{
			throw new MarkupException("Unable to find associated markup file for Border: " +
				this.toString());
		}

		// Find <wicket:border>
		int i;
		for (i = 0; i < markup.size(); i++)
		{
			MarkupElement elem = markup.get(i);
			if (elem instanceof WicketTag)
			{
				WicketTag tag = (WicketTag)elem;
				if (tag.isBorderTag())
				{
					break;
				}
			}
		}

		// If child == null, return the markup fragment starting with the <wicket:border> tag
		if (child == null)
		{
			return new MarkupFragment(markup, i);
		}

		// Since we created the body component instance, identifying that we found it is easy.
		if (child == body)
		{
			// Find the markup for the child component. Make sure you use the preset default value.
			return markup.find(null, BODY_ID, i);
		}

		// Find the markup for the child component
		return markup.find(null, child.getId(), i);
	}

	/**
	 * The container to be associated with the &lt;wicket:body&gt; tag
	 */
	public class BorderBodyContainer extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		/** remember the original status of the wicket:body tag */
		private transient boolean wasOpenCloseTag = false;

		/** More easily create a transparent resolver */
		private boolean transparentResolver;

		/**
		 * Constructor
		 * 
		 * @param id
		 */
		public BorderBodyContainer(final String id)
		{
			super(id);
		}

		/**
		 * @see org.apache.wicket.MarkupContainer#isTransparentResolver()
		 */
		@Override
		public final boolean isTransparentResolver()
		{
			return transparentResolver;
		}

		/**
		 * Make the border body transparent
		 * 
		 * @param value
		 */
		public final void setTransparentResolver(final boolean value)
		{
			transparentResolver = value;
		}

		/**
		 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
		 */
		@Override
		protected void onComponentTag(final ComponentTag tag)
		{
			// Convert open-close to open-body-close
			if (tag.getType() == XmlTag.OPEN_CLOSE)
			{
				tag.setType(XmlTag.OPEN);
				tag.setModified(true);
				wasOpenCloseTag = true;
			}

			super.onComponentTag(tag);
		}

		/**
		 * @see org.apache.wicket.MarkupContainer#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
		 *      org.apache.wicket.markup.ComponentTag)
		 */
		@Override
		protected void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
		{
			if (wasOpenCloseTag == false)
			{
				// It is open-preview-close. Only RawMarkup is allowed within
				// the preview region, which gets stripped from output
				markupStream.skipRawMarkup();
			}

			// This check always results in false for normal requests.
			// In case of ajax requests, the markupstream is not reset after the first render, thus
			// the current index of the markup stream points to the element after the body.
			// As a result, no elements are detected and always omitted.
			originalMarkupStream.setCurrentIndex(beginOfBodyIndex);

			super.onComponentTagBody(originalMarkupStream, Border.this.openTag);
		}

		/**
		 * @see org.apache.wicket.Component#getMarkup()
		 */
		@Override
		public IMarkupFragment getMarkup()
		{
			return getParent().getMarkup(new WebComponent(BODY_ID));
		}

		/**
		 * @see org.apache.wicket.MarkupContainer#getMarkup(org.apache.wicket.Component)
		 */
		@Override
		public IMarkupFragment getMarkup(final Component child)
		{
			IMarkupFragment markup = Border.this.getMarkup();
			if (markup == null)
			{
				return null;
			}

			if (child == null)
			{
				return markup;
			}

			return markup.find(null, child.getId(), 0);
		}
	}
}
