/*
 * $Id$ $Revision$
 * $Date$
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

import wicket.Component;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.parser.XmlTag;

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
 * 
 * <pre>
 *      &lt;span wicket:id=&quot;myPanel&quot;&gt;Example input (will be removed)&lt;/span&gt;
 *     
 *      &lt;wicket:fragment wicket:id=&quot;frag1&quot;&gt;panel 1&lt;/wicket:fragment&gt;
 *      &lt;wicket:fragment wicket:id=&quot;frag2&quot;&gt;panel 2&lt;/wicket:fragment&gt;
 * </pre> 
 * <pre>
 *      add(new Fragment(&quot;myPanel1&quot;, &quot;frag1&quot;);
 * </pre>
 * 
 * @author Juergen Donnerstag
 */
public class Fragment extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/** The wicket:id of the associated markup fragment */
	private String markupId;

	/** The 'component' providing the inline markup */
	private Component markupProvider;

	/**
	 * Constructor.
	 * 
	 * @see wicket.Component#Component(String)
	 * 
	 * @param id
	 *            The component id
	 * @param markupId
	 *            The associated id of the associated markup fragment
	 */
	public Fragment(final String id, final String markupId)
	{
		this(id, markupId, null);
	}

	/**
	 * Constructor.
	 * 
	 * @see wicket.Component#Component(String)
	 * 
	 * @param id
	 *            The component id
	 * @param markupId
	 *            The associated id of the associated markup fragment
	 * @param markupProvider
	 *            The component whose markup contains the fragment markup
	 */
	public Fragment(final String id, final String markupId, final Component markupProvider)
	{
		super(id);

		this.markupId = markupId;
		this.markupProvider = markupProvider;

		// FIXME General: implement this feature.
		if (markupProvider != null)
		{
			throw new UnsupportedOperationException("markupProvider parameter is not yet supported");
		}
	}

	/**
	 * The associated markup fragment can be modified
	 * 
	 * @param markupId
	 */
	public final void setMarkupTagReferenceId(final String markupId)
	{
		// FIXME General: does this need to be versioned?
		this.markupId = markupId;
	}

	/**
	 * Make sure we open up open-close tags to open-body-close
	 * 
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		if (tag.isOpenClose())
		{
			tag.setType(XmlTag.OPEN);
		}
		super.onComponentTag(tag);
	}

	/**
	 * 
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		// Skip the components body. It will be replaced by the fragment
		markupStream.skipRawMarkup();

		// remember the current position in the markup. Will have to come back
		// to it.
		int currentIndex = markupStream.getCurrentIndex();

		// Find the markup fragment
		int index = markupStream.findComponentIndex(null, markupId);
		if (index == -1)
		{
			throw new MarkupException("Markup does not contain a fragment with id=" + markupId
					+ "; Component: " + toString());
		}

		// Set the markup stream position to where the fragment begins
		markupStream.setCurrentIndex(index);

		try
		{
			// Get the fragments open tag
			ComponentTag fragmentOpenTag = markupStream.getTag();

			// We'll completely ignore the fragments open tag. It'll not be
			// rendered
			markupStream.next();

			// Render the body of the fragment
			super.onComponentTagBody(markupStream, fragmentOpenTag);
		}
		finally
		{
			// Make sure the markup stream is positioned where we started back
			// at the original component
			markupStream.setCurrentIndex(currentIndex);
		}
	}
}
