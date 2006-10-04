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

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.IAlternateParentProvider;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
import wicket.markup.html.internal.HeaderContainer;
import wicket.markup.parser.XmlTag;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.markup.resolver.IComponentResolver;
import wicket.model.IModel;

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
 *  &lt;html&gt;
 *  &lt;body&gt;
 *    &lt;wicket:border&gt;
 *      First &lt;wicket:body/&gt; Last
 *    &lt;/wicket:border&gt;
 *  &lt;/body&gt;
 *  &lt;/html&gt;
 * </pre>
 * 
 * And the border was used on a page like this:
 * 
 * <pre>
 *  &lt;html&gt;
 *  &lt;body&gt;
 *    &lt;span wicket:id = &quot;myBorder&quot;&gt;
 *      Middle
 *    &lt;/span&gt;
 *  &lt;/body&gt;
 *  &lt;/html&gt;
 * </pre>
 * 
 * Then the resulting HTML would look like this:
 * 
 * <pre>
 *  &lt;html&gt;
 *  &lt;body&gt;
 *    First Middle Last
 *  &lt;/body&gt;
 *  &lt;/html&gt;
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
 * @author Juergen Donnerstag
 */
public abstract class Border<T> extends WebMarkupContainerWithAssociatedMarkup<T>
		implements
			IComponentResolver,
			IAlternateParentProvider
{
	private static final String BODY = "body";
	private static final String BORDER = "border";

	static
	{
		// register "wicket:fragement"
		WicketTagIdentifier.registerWellKnownTagName(BORDER);
		WicketTagIdentifier.registerWellKnownTagName(BODY);
	}

	/** The border's wicket:body container */
	private MarkupContainer body;

	/**
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public Border(MarkupContainer parent, final String id)
	{
		this(parent, id, null);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public Border(MarkupContainer parent, final String id, final IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * Get the Border's body container. Might be null, if not yet created.
	 * 
	 * @return MarkupContainer
	 */
	protected final MarkupContainer getBodyContainer()
	{
		return this.body;
	}

	/**
	 * 
	 * @see wicket.markup.IAlternateParentProvider#getAlternateParent(java.lang.Class,
	 *      java.lang.String)
	 */
	public MarkupContainer getAlternateParent(final Class childClass, final String childId)
	{
		// If, and only if, a body container exists, than redirect new
		// components to become children of the body.
		return (this.body != null ? this.body : this);
	}

	/**
	 * Create a new Border Body container and add it to the 'parent'. If a body
	 * container has already been created, remove that first.
	 * 
	 * @param parent
	 * @return MarkupContainer
	 */
	protected final MarkupContainer newBorderBodyContainer(final MarkupContainer parent)
	{
		if (this.body != null)
		{
			// TODO Check if empty (no child components)
			this.body.remove();
		}

		this.body = new BorderBody(parent);
		return body;
	}

	/**
	 * 
	 * @see wicket.markup.html.MarkupContainer#getMarkupFragment(java.lang.String)
	 */
	@Override
	public MarkupFragment getMarkupFragment(final String path)
	{
		// First try the associated markup
		MarkupFragment fragment = getAssociatedMarkup(false);
		if (fragment != null)
		{
			fragment = fragment.getChildFragment(path, false);
		}

		// If not yet found, than try the <span wicket:id="myBorder> body
		if (fragment == null)
		{
			fragment = super.getMarkupFragment(path);
		}

		return fragment;
	}

	/**
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	protected final void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		// Render the associated markup
		renderAssociatedMarkup(BORDER,
				"Markup for a border component must begin a tag like '<wicket:border>'");

		// Skip the whole border markup: <span wicket:id="myBorder> ... </span>
		markupStream.skipToMatchingCloseTag(openTag);
	}

	/**
	 * 
	 * @see wicket.markup.resolver.IComponentResolver#resolve(wicket.MarkupContainer,
	 *      wicket.markup.MarkupStream, wicket.markup.ComponentTag)
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		// If wicket:body container has not yet been created and 'tag' is
		// wicket:body, than automatically create and render it.
		if ((this.body == null) && tag.isWicketBodyTag())
		{
			// The body container become the child of 'container', which if an
			// alternate parent has been provided, may not be the border itself,
			// but a (grand-) child of him.
			this.body = newBorderBodyContainer(container);

			// Render the body and its children
			this.body.render(markupStream);
			return true;
		}

		// Unable to resolve the request
		return false;
	}

	/**
	 * 
	 * @see wicket.Component#renderHead(wicket.markup.html.internal.HeaderContainer)
	 */
	@Override
	public void renderHead(final HeaderContainer container)
	{
		if (isHeadRendered() == false)
		{
			this.renderHeadFromAssociatedMarkupFile(container);
		}
		super.renderHead(container);
	}

	/**
	 * The wicket:body container
	 */
	private class BorderBody extends WebMarkupContainer implements IComponentResolver
	{
		private static final long serialVersionUID = 1L;

		// True, if <wicket:body/>
		private transient boolean wasOpenClose = false;

		/**
		 * Construct.
		 * 
		 * @param parent
		 */
		public BorderBody(final MarkupContainer parent)
		{
			super(parent, Component.AUTO_COMPONENT_PREFIX + BODY);

			// Make sure the fragment is attached which does not automatically
			// happen with auto-components.
			getMarkupFragment();
		}

		/**
		 * Get the wicket:body fragment
		 * 
		 * @see wicket.Component#getMarkupFragment()
		 */
		@Override
		public MarkupFragment getMarkupFragment()
		{
			MarkupContainer parent = getParent();
			MarkupFragment fragment;

			// If the parent is a Border, than search for <wicket:body> in the
			// associated markup
			if (parent instanceof Border)
			{
				fragment = Border.this.getAssociatedMarkup(true).getWicketFragment(BORDER, true);
			}
			else
			{
				// If the parent is not a Border, than at least one
				// MarkupContainer is in between the Border and the Body
				fragment = parent.getMarkupFragment();
			}

			// Search for the <wicket:body> tag
			return fragment.getWicketFragment(BODY, true);
		}

		/**
		 * @see wicket.MarkupContainer#getMarkupFragment(java.lang.String)
		 */
		@Override
		protected MarkupFragment getMarkupFragment(final String id)
		{
			// First search for the 'id' in the <span
			// wicket:id="myBorder">...</span> markup
			MarkupFragment fragment = Border.this.getMarkupFragment(id);
			if (fragment != null)
			{
				return fragment;
			}

			// Else, check for the 'id' in the border's associated markup
			return Border.this.getAssociatedMarkup(true).getWicketFragment(BORDER, true)
					.getChildFragment(id, true);
		}

		/**
		 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
		 */
		@Override
		protected void onComponentTag(final ComponentTag tag)
		{
			// Automatically change the tag from OpenClose to Open-Body-Close if
			// needed
			if (tag.isOpenClose())
			{
				tag.setType(XmlTag.Type.OPEN);
				this.wasOpenClose = true;
			}

			super.onComponentTag(tag);
		}

		/**
		 * 
		 * @see wicket.MarkupContainer#onComponentTagBody(wicket.markup.MarkupStream,
		 *      wicket.markup.ComponentTag)
		 */
		@Override
		protected void onComponentTagBody(final MarkupStream markupStream,
				final ComponentTag openTag)
		{
			// Get the body's markup which is equal to the border's markup
			// <span wicket:id="myBorder"> xxxx </span>
			MarkupFragment borderFragment = Border.this.getMarkupFragment();
			MarkupStream borderMarkupStream = new MarkupStream(borderFragment);

			// Skip the <span wicket:id="myBorder> tag
			borderMarkupStream.next();

			// Render wicket:body and its children
			renderComponentTagBody(borderMarkupStream, borderFragment.getTag(0));

			// If the open tag was Open-Body-Close than remove the body. The
			// body raw markup might be used as none-computed preview text.
			if (wasOpenClose == false)
			{
				markupStream.skipRawMarkup();
			}
		}

		/**
		 * @see wicket.markup.resolver.IComponentResolver#resolve(wicket.MarkupContainer,
		 *      wicket.markup.MarkupStream, wicket.markup.ComponentTag)
		 */
		public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
				final ComponentTag tag)
		{
			// The body container is semi-transparent. It is only transparent
			// for none wicket:body tags

			// If wicket:body, than we definitely can't help you
			if (tag.isWicketBodyTag())
			{
				return false;
			}

			// For none wicket:body tags behave like a transparent container.
			// Check if the parent is able to resolve the wicket:id. If yes,
			// than render the component.
			final Component child = getParent().get(tag.getId());
			if (child != null)
			{
				child.render(markupStream);
				return true;
			}

			// Unable to resolve the request
			return false;
		}
	}
}
