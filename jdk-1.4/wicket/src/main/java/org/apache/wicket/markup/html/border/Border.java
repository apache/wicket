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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.markup.resolver.BorderBodyResolver;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.IModel;

/**
 * A border component has associated markup which is drawn and determines
 * placement of any markup and/or components nested within the border component.
 * <p>
 * The portion of the border's associated markup file which is to be used in
 * rendering the border is denoted by a &lt;wicket:border&gt; tag. The children
 * of the border component instance are then inserted into this markup,
 * replacing the first &lt;wicket:body&gt; tag in the border's associated
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
 * In other words, the body of the myBorder component is substituted into the
 * border's associated markup at the position indicated by the
 * &lt;wicket:body&gt; tag.
 * <p>
 * Regarding &lt;wicket:body/&gt; you have two options. Either use
 * &lt;wicket:body/&gt; (open-close tag) which will automatically be expanded to
 * &lt;wicket:body&gt;body content&lt;/wicket:body&gt; or use
 * &lt;wicket:body&gt;preview region&lt;/wicket:body&gt; in your border's
 * markup. The preview region (everything in between the open and close tag)
 * will automatically be removed.
 * 
 * @see BorderBodyResolver
 * @see BorderBodyContainer
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public abstract class Border extends WebMarkupContainerWithAssociatedMarkup
		implements
			IComponentResolver
{
	static final String BODY = "body";
	static final String BORDER = "border";

	static
	{
		// register "wicket:body" and "wicket:border"
		WicketTagIdentifier.registerWellKnownTagName(BORDER);
		WicketTagIdentifier.registerWellKnownTagName(BODY);
	}

	/** Should be true for bordered pages */
	private boolean transparentResolver = false;

	/** Must be the same as the id automatically assigned to <wicket:body> */
	private final String BODY_ID = "_body";

	/** The body component associated with <wicket:body> */
	private BorderBodyContainer body;

	/**
	 * only required during render phase. The markup stream associated with
	 * <span wicket:id="myBorder"
	 */
	private transient MarkupStream originalMarkupStream;

	/** only required during render phase. The <span wicket:id="myBorder"> tag */
	private transient ComponentTag openTag;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public Border(final String id)
	{
		super(id);

		body = new BorderBodyContainer(BODY_ID);
		add(body);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Border(final String id, final IModel model)
	{
		super(id, model);

		body = new BorderBodyContainer(BODY_ID);
		add(body);
	}

	/**
	 * Gets the container associated with &lt;wicket:body&gt;
	 * 
	 * @return The border body container
	 */
	public final BorderBodyContainer getBodyContainer()
	{
		if (body == null)
		{
			body = (BorderBodyContainer)get(BODY_ID);
		}
		return body;
	}

	/**
	 * When this method is called with a false value the components and raw
	 * markup that this border wraps will not be rendered.
	 * 
	 * @param bodyVisible
	 * @return this for chaining
	 * @deprecated 1.3 please use #getBodyContainer().setVisible(false) instead
	 */
	public Border setBorderBodyVisible(boolean bodyVisible)
	{
		body.setVisible(false);
		return this;
	}

	/**
	 * Borders used for bordered pages should set it to "true". Default is
	 * "false". If enabled, than requests to find a component are passed to the
	 * parent container as well. Thus the child may not be added to the Border,
	 * but might be added to the parent of the Border as well.
	 * 
	 * @param enable
	 *            true, to enable transparent resolving
	 * @return this for chaining
	 */
	public final Border setTransparentResolver(final boolean enable)
	{
		transparentResolver = enable;
		return this;
	}

	/**
	 * @see #setTransparentResolver(boolean)
	 * @see org.apache.wicket.MarkupContainer#isTransparentResolver()
	 */
	public boolean isTransparentResolver()
	{
		return transparentResolver;
	}

	/**
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
	 *      org.apache.wicket.markup.MarkupStream,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		// In case of nested Borders, the outer border is no longer able to find
		// its body container easily. Thus we need to help resolve it.

		// The container is the body component. Get the Border component.
		MarkupContainer border = container.getParent();
		while ((border != null) && !(border instanceof Border))
		{
			border = border.getParent();
		}

		// Avoid recursions. It is the outer border which needs help to resolve
		// it. Not the inner border (this == border).
		if ((border == null) || (this == border))
		{
			return false;
		}

		// Ignore everything else except Border
		if (!(border instanceof Border))
		{
			return false;
		}

		// Determine if tag is a <wicket:body> tag
		if (!(tag instanceof WicketTag))
		{
			return false;
		}

		// And it must be <wicket:body>
		final WicketTag wtag = (WicketTag)tag;
		if (!wtag.isBodyTag())
		{
			return false;
		}

		body.render(markupStream);
		return true;
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		if (tag.isOpen() == false)
		{
			throw new WicketRuntimeException(
					"The border tag must be an open tag. Open-close is not allowed: " +
							tag.toString());
		}

		super.onComponentTag(tag);
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	protected final void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		// Remember the data for easy access by the Body component
		this.openTag = openTag;
		originalMarkupStream = getMarkupStream();

		// body.isVisible(false) needs a little extra work. We must skip the
		// markup between <span wicket:id="myBorder"> and </span>
		if (body.isVisible() == false)
		{
			originalMarkupStream.skipToMatchingCloseTag(openTag);
		}

		// Render the associated markup
		renderAssociatedMarkup("border",
				"Markup for a border component must begin a tag like '<wicket:border>'");
	}

	/**
	 * @see org.apache.wicket.Component#renderHead(org.apache.wicket.markup.html.internal.HtmlHeaderContainer)
	 */
	public void renderHead(HtmlHeaderContainer container)
	{
		renderHeadFromAssociatedMarkupFile(container);
		super.renderHead(container);
	}

	/**
	 * The container to be associated with the &lt;wicket:body&gt; tag
	 */
	public class BorderBodyContainer extends WebMarkupContainer implements IComponentResolver
	{
		private static final long serialVersionUID = 1L;

		/** remember the original status of the wicket:body tag */
		private transient boolean wasOpenCloseTag = false;

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
		 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
		 */
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
		protected void onComponentTagBody(final MarkupStream markupStream,
				final ComponentTag openTag)
		{
			if (wasOpenCloseTag == false)
			{
				// It is open-preview-close. Only RawMarkup is allowed within
				// the preview region, which gets stripped from output
				markupStream.skipRawMarkup();
			}

			super.onComponentTagBody(originalMarkupStream, Border.this.openTag);
		}

		/**
		 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
		 *      org.apache.wicket.markup.MarkupStream,
		 *      org.apache.wicket.markup.ComponentTag)
		 */
		public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
				final ComponentTag tag)
		{
			// Usually you add child components to Border instead of Body. Hence
			// we need to help Body to properly resolve the children.
			String id = tag.getId();
			if (!id.equals(BODY_ID))
			{
				Component component = Border.this.get(id);
				if (component != null)
				{
					component.render(markupStream);
					return true;
				}
			}

			return false;
		}
	}
}
