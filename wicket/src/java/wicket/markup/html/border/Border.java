/*
 * $Id: Border.java 4831 2006-03-08 13:32:22 -0800 (Wed, 08 Mar 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-03-08 13:32:22 -0800 (Wed, 08 Mar
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.border;

import wicket.MarkupContainer;
import wicket.Response;
import wicket.markup.ComponentTag;
import wicket.markup.IMarkup;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupNotFoundException;
import wicket.markup.MarkupStream;
import wicket.markup.html.IMarkupProvider;
import wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
import wicket.markup.html.internal.HeaderContainer;
import wicket.markup.parser.XmlTag;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.markup.resolver.IComponentResolver;
import wicket.model.IModel;
import wicket.response.NullResponse;

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
 * &lt;html&gt;
 * &lt;body&gt;
 *   &lt;wicket:border&gt;
 *     First &lt;wicket:body/&gt; Last
 *   &lt;/wicket:border&gt;
 * &lt;/body&gt;
 * &lt;/html&gt;
 * </pre>
 * 
 * And the border was used on a page like this:
 * 
 * <pre>
 * &lt;html&gt;
 * &lt;body&gt;
 *   &lt;span wicket:id = &quot;myBorder&quot;&gt;
 *     Middle
 *   &lt;/span&gt;
 * &lt;/body&gt;
 * &lt;/html&gt;
 * </pre>
 * 
 * Then the resulting HTML would look like this:
 * 
 * <pre>
 * &lt;html&gt;
 * &lt;body&gt;
 *   First Middle Last
 * &lt;/body&gt;
 * &lt;/html&gt;
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
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Jonathan Locke
 */
public abstract class Border<T> extends WebMarkupContainerWithAssociatedMarkup<T>
		implements
			IComponentResolver
{
	static
	{
		// register "wicket:fragement"
		WicketTagIdentifier.registerWellKnownTagName("border");
		WicketTagIdentifier.registerWellKnownTagName("body");
	}

	/** Will be true, once the first <wicket:body> has been seen */
	private transient boolean haveSeenBodyTag = false;

	/** The open tag for this border component. */
	private transient ComponentTag openTag;

	/** Should be true for bordered pages */
	private boolean transparentResolver = false;

	/** If false, the content of <wicket:body> will not be printed */
	private boolean bodyVisible = true;

	/**
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public Border(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public Border(MarkupContainer parent, final String id, final IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * When this method is called with a false value the components and raw
	 * markup that this border wraps will not be rendered.
	 * 
	 * @param bodyVisible
	 * @return this for chaining
	 */
	public Border setBorderBodyVisible(boolean bodyVisible)
	{
		this.bodyVisible = bodyVisible;
		return this;
	}

	/**
	 * 
	 * @see wicket.MarkupContainer#isTransparentResolver()
	 */
	@Override
	public boolean isTransparentResolver()
	{
		return transparentResolver;
	}

	/**
	 * Borders used for bordered pages should set it to "true". Default is
	 * "false".
	 * 
	 * @param transparentResolver
	 * @return this for chaining
	 */
	public final Border setTransparentResolver(final boolean transparentResolver)
	{
		this.transparentResolver = transparentResolver;
		return this;
	}

	/**
	 * Like Panels, Borders have associated Markup files and hence must
	 * implement IMarkupProvider. But Border are different in that they allow to
	 * have child components with markup either in the associated markup file
	 * (between the wicket:border and wicket:body tags) or the span tag which
	 * declares the border component.
	 * 
	 * @see wicket.markup.html.WebMarkupContainerWithAssociatedMarkup#getMarkupFragment(java.lang.String)
	 */
	@Override
	public MarkupFragment getMarkupFragment(String path)
	{
		// First try to find the markup associated with 'path' in the external
		// markup file
		try
		{
			return super.getMarkupFragment(path);
		}
		catch (RuntimeException ex)
		{
			// ignore
		}

		// If not found in the external markup file, than try the markup which
		// contains the <span wicket:id="myBorder> tag.
		path = getId() + IMarkup.TAG_PATH_SEPARATOR + path;
		
		// The markup path must be relativ to the markup file, hence we need to
		// find the first parent with associated markup file and update the
		// markup path accordingly.
		MarkupContainer parent = getParent();
		while ((parent != null) && !(parent instanceof IMarkupProvider))
		{
			path = parent.getMarkupFragmentPath(path);
			parent = parent.getParent();
		}

		if (parent == null)
		{
			throw new MarkupNotFoundException("Component has no parent with external markup file: "
					+ getId());
		}

		// We found the markup file and created the markup path. Now go and get
		// the fragment.
		MarkupFragment fragment = ((IMarkupProvider)parent).getMarkupFragment(path);
		if (fragment == null)
		{
			throw new MarkupNotFoundException(
					"Unable to find the markup fragment with markup path '" + path
							+ "'. Component: " + getId());
		}

		return fragment;
	}

	/**
	 * Border makes use of a &lt;wicket:body&gt; tag to identify the position to
	 * insert within the border's body. As &lt;wicket:body&gt; is a special tag
	 * and MarkupContainer is not able to handle it, we do that here.
	 * <p>
	 * You have two options. Either use &lt;wicket:body/&gt; (open-close tag)
	 * which will automatically be expanded to &lt;wicket:body&gt;body
	 * content&lt;/wicket:body&gt; or use &lt;wicket:body&gt;preview
	 * region&lt;/wicket:body&gt; in your border's markup. The preview region
	 * (everything in between the open and close tag) will automatically be
	 * removed.
	 * 
	 * @see IComponentResolver#resolve(MarkupContainer, MarkupStream,
	 *      ComponentTag)
	 * 
	 * @param container
	 *            The container parsing its markup
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @return True if componentId was handled by the resolver, false otherwise.
	 */
	public final boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		// Determine if tag is a <wicket:body> tag
		if (!tag.isWicketBodyTag())
		{
			return false;
		}

		final Response originalResponse;
		if (this.bodyVisible == true)
		{
			originalResponse = null;
		}
		else
		{
			originalResponse = getRequestCycle().setResponse(NullResponse.getInstance());
		}

		try
		{
			renderBodyComponent(markupStream, tag);
		}
		finally
		{
			if (originalResponse != null)
			{
				getRequestCycle().setResponse(originalResponse);
			}
		}

		return true;
	}

	/**
	 * Render the tag body
	 * 
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	protected final void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		// Save open tag for callback later to render body
		this.openTag = openTag;

		// initialize
		this.haveSeenBodyTag = false;

		// Render the associated markup
		renderAssociatedMarkup("border",
				"Markup for a border component must begin a tag like '<wicket:border>'");

		// There shall exactly only one body tag per border
		if (haveSeenBodyTag == false)
		{
			markupStream
					.throwMarkupException("Didn't find <wicket:body/> tag for the border compoment.");
		}
	}

	/**
	 * 
	 * @see wicket.Component#renderHead(wicket.markup.html.internal.HeaderContainer)
	 */
	@Override
	public void renderHead(HeaderContainer container)
	{
		if (isHeadRendered() == false)
		{
			this.renderHeadFromAssociatedMarkupFile(container);
		}
		super.renderHead(container);
	}

	/**
	 * Render the wicket:body and all what is in it.
	 * 
	 * @param markupStream
	 *            The associated markup stream
	 * @param wtag
	 *            The wicket:body tag
	 */
	public void renderBodyComponent(final MarkupStream markupStream, final ComponentTag wtag)
	{
		// Ok, it is a wicket:body tag. Now render its body
		final ComponentTag bodyTag = renderBodyComponentTag(markupStream, wtag);

		// If markup stream is null, that indicates we already recursed into
		// this block of log and set it to null (below). If we did that,
		// then we want to go up another level of border nesting.
		Border<?> border = this;
		if (border.getMarkupStream() == null)
		{
			// Find Border at or above parent of this border
			final MarkupContainer<?> borderParent = border.getParent();
			border = (Border)((borderParent instanceof Border) ? borderParent : borderParent
					.findParent(Border.class));
		}

		// Get the border's markup
		final MarkupStream borderMarkup = border.findMarkupStream();

		// Set markup of border to null. This allows us to find the border's
		// parent's markup. It also indicates that we've been here in the
		// log just above.
		border.setMarkupStream(null);

		// Draw the children of the border component using its original
		// in-line markup stream (not the border's associated markup stream)
		border.renderComponentTagBody(border.findMarkupStream(), border.openTag);

		// Restore border markup so it can continue rendering
		border.setMarkupStream(borderMarkup);

		// Render body close tag: </wicket:body>
		if (wtag.isOpenClose())
		{
			markupStream.next();
			bodyTag.setType(XmlTag.Type.CLOSE);
			renderComponentTag(bodyTag);
		}

		// There shall exactly only one body tag per border
		if (border.haveSeenBodyTag == true)
		{
			markupStream
					.throwMarkupException("There must be exactly one <wicket:body> tag for each border compoment.");
		}

		border.haveSeenBodyTag = true;
	}

	/**
	 * Render the wicket:body tag
	 * 
	 * @param markupStream
	 *            The associated markup stream
	 * @param tag
	 *            The wicket:body tag
	 */
	public void renderBodyComponentTagBody(final MarkupStream markupStream, final ComponentTag tag)
	{
		renderComponentTagBody(markupStream, tag);
	}

	/**
	 * Render the wicket:body tag
	 * 
	 * @param tag
	 *            The wicket:body tag
	 * @param markupStream
	 *            The associated markup stream
	 * @return the body tag. May be its type has been changed
	 */
	protected ComponentTag renderBodyComponentTag(final MarkupStream markupStream,
			final ComponentTag tag)
	{
		ComponentTag bodyTag = tag;

		// Ok, it is a wicket:body tag. Now render its body
		if (tag.isOpen())
		{
			// It is open-preview-close already.
			// Only RawMarkup is allowed within the preview region, which
			// gets stripped from output
			markupStream.next();
			markupStream.skipRawMarkup();
		}
		else if (tag.isOpenClose())
		{
			// Automatically expand <wicket:body/> to
			// <wicket:body>...</wicket:body>
			// in order for the html to look right: insert the body in between
			// the wicket tags instead of behind the open-close tag.
			bodyTag = tag.mutable();
			bodyTag.setType(XmlTag.Type.OPEN);
		}
		else
		{
			markupStream
					.throwMarkupException("A <wicket:body> tag must be an open or open-close tag.");
		}

		renderComponentTag(bodyTag);
		return bodyTag;
	}
}
