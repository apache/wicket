/*
 * $Id: Fragment.java 5075 2006-03-21 18:59:42 -0800 (Tue, 21 Mar 2006)
 * ivaynberg $ $Revision$ $Date: 2006-03-21 18:59:42 -0800 (Tue, 21 Mar
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
package wicket.markup.html.panel;

import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupStream;
import wicket.markup.html.IMarkupProvider;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.parser.XmlTag;
import wicket.model.IModel;
import wicket.util.lang.Objects;
import wicket.version.undo.Change;

/**
 * Usually you either have a markup file or a xml tag with
 * wicket:id="myComponent" to associate markup with a component. However in some
 * rare cases, especially when working with small panels it is a bit awkward to
 * maintain tiny pieces of markup in plenty of panel markup files. Use cases are
 * for example list views where list items are different depending on a state.
 * <p>
 * Fragments provide a means to maintain the panels tiny piece of markup in the
 * parents markup file.
 * <p>
 * <code>markupProvider</code> is used to specify which component's markup
 * contains the definition of the fragment. <code>markupProvider</code> should
 * either be Panel, Border, or Page. The markup lookup wil be performed in the
 * markup file belonging to that component, eg if a Panel is specified that
 * panel's <code>[markupProvider.getClass().getName()].html</code> file will
 * be searched for the wicket:fragment tag with the appropriate id.
 * 
 * <pre>
 *            &lt;span wicket:id=&quot;myPanel&quot;&gt;Example input (will be removed)&lt;/span&gt;
 *                         
 *            &lt;wicket:fragment wicket:id=&quot;frag1&quot;&gt;panel 1&lt;/wicket:fragment&gt;
 *            &lt;wicket:fragment wicket:id=&quot;frag2&quot;&gt;panel 2&lt;/wicket:fragment&gt;
 * </pre> 
 * <pre>
 *            add(new Fragment(&quot;myPanel1&quot;, &quot;frag1&quot;);
 * </pre>
 * 
 * @param <T>
 *            The type of the model object
 * 
 * @author Juergen Donnerstag
 */
public class Fragment<T> extends WebMarkupContainer<T> implements IMarkupProvider
{
	private static final long serialVersionUID = 1L;

	/** The wicket:id of the associated markup fragment */
	private String markupId;

	/**
	 * The container providing the inline markup. If null, than the fragment's
	 * parent is assumed to provide the markup
	 */
	private MarkupContainer markupProvider;

	/**
	 * Constructor.
	 * 
	 * @see wicket.Component#Component(MarkupContainer,String)
	 * 
	 * @param id
	 *            The component id
	 * @param markupId
	 *            The associated id of the associated markup fragment
	 */
	public Fragment(final MarkupContainer parent, final String id, final String markupId)
	{
		this(parent, id, markupId, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @see wicket.Component#Component(MarkupContainer,String)
	 * 
	 * @param id
	 *            The component id
	 * @param markupId
	 *            The associated id of the associated markup fragment
	 * @param model
	 *            The model for this fragment
	 */
	public Fragment(final MarkupContainer parent, final String id, final String markupId,
			final IModel<T> model)
	{
		this(parent, id, markupId, null, model);
	}

	/**
	 * Constructor.
	 * 
	 * @see wicket.Component#Component(MarkupContainer,String)
	 * 
	 * @param id
	 *            The component id
	 * @param markupId
	 *            The associated id of the associated markup fragment. See
	 *            javadoc on the class for more details.
	 * @param markupProvider
	 *            The component whose markup contains the fragment's markup
	 */
	public Fragment(final MarkupContainer parent, final String id, final String markupId,
			final MarkupContainer markupProvider)
	{
		this(parent, id, markupId, markupProvider, null);
	}

	/**
	 * Constructor.
	 * 
	 * @see wicket.Component#Component(MarkupContainer,String)
	 * 
	 * @param id
	 *            The component id
	 * @param markupId
	 *            The associated id of the associated markup fragment
	 * @param markupProvider
	 *            The component whose markup contains the fragment's markup. See
	 *            javadoc on the class for more details.
	 * @param model
	 *            The model for this fragment
	 */
	public Fragment(final MarkupContainer parent, final String id, final String markupId,
			final MarkupContainer markupProvider, final IModel<T> model)
	{
		super(parent, id, model);

		if (markupId == null)
		{
			throw new IllegalArgumentException("Parameter 'markupId' cannot be null");
		}

		this.markupId = markupId;
		this.markupProvider = markupProvider;
	}

	/**
	 * @return The fragment's markup id in the parent markup.
	 */
	public final String getFragmentMarkupId()
	{
		return markupId;
	}

	/**
	 * The associated markup fragment can be modified
	 * 
	 * @param markupId
	 */
	public final void setMarkupTagReferenceId(final String markupId)
	{
		if (markupId == null)
		{
			throw new IllegalArgumentException("markupId cannot be null");
		}

		// Make sure the associated markup exists
		loadMarkupStream();

		if (!Objects.equal(this.markupId, markupId))
		{
			addStateChange(new Change()
			{
				private static final long serialVersionUID = 1L;
				private final String oldMarkupId = Fragment.this.markupId;

				@Override
				public void undo()
				{
					Fragment.this.markupId = oldMarkupId;
				}
			});
		}
		this.markupId = markupId;
	}

	/**
	 * Make sure we open up open-close tags to open-body-close
	 * 
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		if (tag.isOpenClose())
		{
			tag.setType(XmlTag.Type.OPEN);
		}
		super.onComponentTag(tag);
	}

	/**
	 * 
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		// Skip the components body. It will be replaced by the fragment
		markupStream.skipRawMarkup();

		final MarkupStream providerMarkupStream = chooseMarkupStream(markupStream);
		if (providerMarkupStream == null)
		{
			throw new IllegalStateException(
					"no markup stream found for providing markup container " + markupProvider);
		}

		renderFragment(providerMarkupStream, openTag);
	}

	/**
	 * Get the markup stream which shall be used to search for the fragment
	 * 
	 * @param markupStream
	 *            The markup stream is associated with the component (not the
	 *            fragment)
	 * @return The markup stream to be used to find the fragment markup
	 */
	protected MarkupStream chooseMarkupStream(final MarkupStream markupStream)
	{
		if (this.markupProvider == null)
		{
			return markupStream;
		}

		// The following statement assumes that the markup provider is a
		// parent along the line up to the Page
		return this.markupProvider.getMarkupStream();
	}

	/**
	 * Render the markup starting at the current position of the markup strean
	 * 
	 * @see #onComponentTagBody(MarkupStream, ComponentTag)
	 * 
	 * @param providerMarkupStream
	 * @param openTag
	 */
	private void renderFragment(final MarkupStream providerMarkupStream, final ComponentTag openTag)
	{
		// remember the current position in the markup. Will have to come back
		// to it.
		int currentIndex = providerMarkupStream.getCurrentIndex();

		// Find the markup fragment
		int index = providerMarkupStream.positionAt(markupId, true);

		try
		{
			// Get the fragments open tag
			ComponentTag fragmentOpenTag = providerMarkupStream.getTag();

			// We'll completely ignore the fragments open tag. It'll not be
			// rendered
			providerMarkupStream.next();

			// Render the body of the fragment
			super.onComponentTagBody(providerMarkupStream, fragmentOpenTag);
		}
		finally
		{
			// Make sure the markup stream is positioned where we started back
			// at the original component
			providerMarkupStream.setCurrentIndex(currentIndex);
		}
	}

	/**
	 * @return markup provider or null if not set
	 */
	public final MarkupContainer getMarkupProvider()
	{
		return markupProvider;
	}

	/**
	 * Components which have associated markup files or which need special
	 * treatment to find the markup, such as Fragment, must implement
	 * IMarkupProvider. Fragment is similar to Panel, which has an associated
	 * markup file, and hence requires to implement IMarkupProvider as well.
	 * 
	 * @see wicket.markup.html.IMarkupProvider#getMarkupFragment(java.lang.String)
	 */
	public MarkupFragment getMarkupFragment(final String path)
	{
		// If markupProvider == null, than we assume the markup fragment to be
		// in the fragment's parent markup.
		if (this.markupProvider == null)
		{
			// Find the Fragment's parent with associated markup file
			MarkupContainer parent = getParent();
			while ((parent != null) && !(parent instanceof IMarkupProvider))
			{
				parent = parent.getParent();
			}

			if (parent == null)
			{
				throw new WicketRuntimeException(
						"Component has no parent with external markup file: " + getId());
			}

			// Find the markup fragment with the markup path provided by the
			// user
			MarkupFragment fragment = ((IMarkupProvider)parent).getMarkupFragment(this.markupId);
			if (fragment == null)
			{
				throw new WicketRuntimeException(
						"Unable to find the markup fragment with markup path '" + path
								+ "'. Component: " + getId());
			}

			return fragment;
		}

		// Find the fragment based on the markup path and the Container provided
		// by the user
		MarkupFragment fragment = this.markupProvider.getMarkupFragment().getChildFragment(
				this.markupId);
		if (fragment == null)
		{
			throw new WicketRuntimeException(
					"Unable to find the markup fragment with markup path '" + path
							+ "' in markup provided by container: " + this.markupProvider.getId()
							+ ". Component: " + getId());
		}

		return fragment;
	}
}
