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
import wicket.Page;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.border.Border;
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
 *    &lt;span wicket:id=&quot;myPanel&quot;&gt;Example input (will be removed)&lt;/span&gt;
 *                          
 *    &lt;wicket:fragment wicket:id=&quot;frag1&quot;&gt;panel 1&lt;/wicket:fragment&gt;
 *    &lt;wicket:fragment wicket:id=&quot;frag2&quot;&gt;panel 2&lt;/wicket:fragment&gt;
 * </pre> 
 * <pre>
 *    add(new Fragment(&quot;myPanel1&quot;, &quot;frag1&quot;);
 * </pre>
 * 
 * @param <T>
 *            The type of the model object
 * 
 * @author Juergen Donnerstag
 * @author Igor Vaynberg (ivaynberg)
 */
public class Fragment<T> extends WebMarkupContainer<T>
{
	private static final long serialVersionUID = 1L;

	/** The wicket:id of the associated markup fragment */
	private String markupId;

	/**
	 * The container providing the inline markup. If null, than the fragment's
	 * parent is assumed to provide the markup
	 */
	private MarkupContainer markupProvider;

	/** The markup associated with the Fragment. Make transient to better support Clusters */
	private transient MarkupFragment fragment;
	
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

		if ((markupProvider != null) && !(markupProvider instanceof Panel)
				&& !(markupProvider instanceof Page) && !(markupProvider instanceof Border))
		{
			throw new IllegalArgumentException(
					"Argument [[markupProvider]] must be an instance of one of the following types: "
							+ "Page, Panel, and Border. Currently it is of type [["
							+ markupProvider.getClass().getName() + "]]");
		}

		this.markupId = markupId;
		this.markupProvider = markupProvider;
		if (this.markupProvider == null)
		{
			this.markupProvider = this.findParentWithAssociatedMarkup();
		}
		
		this.fragment = getFragment();
	}

	/**
	 * 
	 * @return The Fragments markup
	 */
	private MarkupFragment getFragment()
	{
		MarkupFragment fragment = this.markupProvider.getAssociatedMarkup(false);
		if (fragment == null)
		{
			fragment = this.markupProvider.getMarkupFragment();
		}
		fragment = fragment.getChildFragment(markupId, true);
		return fragment;
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
		this.fragment = getFragment();

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
	 * 
	 * @see wicket.MarkupContainer#getMarkupFragment(java.lang.String)
	 */
	protected MarkupFragment getMarkupFragment(final String id)
	{
		if (this.fragment == null)
		{
			this.fragment = getFragment();
		}
		return this.fragment.getChildFragment(id, true);
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

		if (this.fragment == null)
		{
			this.fragment = getFragment();
		}
		
		final MarkupStream providerMarkupStream = new MarkupStream(this.fragment);
		if (providerMarkupStream == null)
		{
			throw new IllegalStateException(
					"no markup stream found for providing markup container " + markupProvider);
		}

		renderFragment(providerMarkupStream, openTag);
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
		// Get the fragments open tag
		ComponentTag fragmentOpenTag = providerMarkupStream.getTag();

		// We'll completely ignore the fragments open tag. It'll not be
		// rendered
		providerMarkupStream.next();

		// Render the body of the fragment
		super.onComponentTagBody(providerMarkupStream, fragmentOpenTag);
	}

	/**
	 * @return markup provider or null if not set
	 */
	public final MarkupContainer getMarkupProvider()
	{
		return markupProvider;
	}
}
