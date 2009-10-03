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
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupFragment;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.IModel;

/**
 * A border component has associated markup which is drawn and determines placement of any markup
 * and/or components nested within the border component.
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
 * Two convenience methods {@link #addToBorderBody(Component...)} and {@link #getFromBorderBody(String)} are
 * provided to add or get components to the body container.
 * 
 * This implementation does not apply any magic with respect to component handling. In doubt think
 * simple.
 * <p/>
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @since 1.5
 */
public abstract class BaseBorder extends WebMarkupContainerWithAssociatedMarkup
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
	private final MarkupContainer body;

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
	public BaseBorder(final String id)
	{
		this(id, (IModel<?>)null);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public BaseBorder(final String id, final IModel<?> model)
	{
		super(id, model);

		body = newBorderBodyContainer(id + BODY_ID);
		add(body);
	}

	/**
	 * Gets the container associated with &lt;wicket:body&gt;
	 * 
	 * @return The border body container
	 */
	public final MarkupContainer getBodyContainer()
	{
		return body;
	}

	/**
	 * Create a new BorderBodyContainer
	 * 
	 * @param id
	 * @return A new instance
	 */
	MarkupContainer newBorderBodyContainer(final String id)
	{
		return new BorderBodyContainer(id);
	}

	/**
	 * As a user of the Border, this is probably what you want to call to add a component to the
	 * border. This is for all components which have been added to the markup like this:
	 * 
	 * <pre>
	 *   &lt;span wicket:id="myBorder"&gt;
	 *     &lt;input wicket:id="text1" .. /&gt;
	 *     ...
	 *   &lt;/span&gt;
	 * </pre>
	 * 
	 * Whereas {@link #add(Component...)} will add a component associated with the following markup:
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
	public BaseBorder addToBorderBody(final Component... child)
	{
		getBodyContainer().add(child);
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
		if (rendering == false)
		{
			if (BODY_ID.equals(tag.getId()))
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
		// Than check if markup can be found in between <wicket:border>...</wicket:border>
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

		if (child == body)
		{
			// Find the markup for the child component
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

			// this check always results in false for normal requests.
			// in case of ajax requests, the markupstream is not reset after the first render, thus
			// the current index of the markup stream points to the element after the body.
			// as a result, no elements are detected and always omitted.
			originalMarkupStream.setCurrentIndex(beginOfBodyIndex);

			super.onComponentTagBody(originalMarkupStream, BaseBorder.this.openTag);
		}

		/**
		 * The implementation is against the rule that getMarkup() returns the components markup
		 * which in this case would be something like <code>&lt;wicket:body/&gt;</code>. But that
		 * doesn't work if the body container has been added to some kind of wrapper (e.g. Form,
		 * another Border, etc.). The reason is that the body container's id and the tag id are
		 * different. The tag's id always is "_body" where as the body's id is something like
		 * border.id + "_body". And the reason for that is the Page object, and thus
		 * Page#getAutoIndex(), not being available when needed in the constructor. So
		 * BaseBorder.BorderBodyContainer is an exception in that it returns what you would expect
		 * from getMarkup(null). But don't worry, via
		 * <code>getBodyContainer().getParent().getMarkup(new WebComponent("_body"));</code> you can
		 * still get hold of the <code>&lt;wicket:body/&gt;</code> markup if really needed.
		 * 
		 * @see org.apache.wicket.Component#getMarkup()
		 */
		@Override
		public IMarkupFragment getMarkup()
		{
			return BaseBorder.this.getMarkup();
		}
	}
}
