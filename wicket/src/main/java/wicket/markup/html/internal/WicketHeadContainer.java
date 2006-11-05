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
package wicket.markup.html.internal;

import wicket.MarkupContainer;
import wicket.Response;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.response.NullResponse;

/**
 * For each wicket:head tag a WicketHeadContainer is created and added to the
 * HtmlHeaderContainer which has been added to the Page.
 * 
 * @author Juergen Donnerstag
 */
public final class WicketHeadContainer extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/** The scope attribute of the wicket:head tag */
	private String scope;

	/** True if wicket:head and its child components are to be rendered */
	private boolean enable = true;

	/**
	 * The (auto-)component is created during the render phase and removed at
	 * the end of the render phase
	 */
	private transient MarkupFragment fragment;

	/**
	 * @param parent
	 *            The owner parent of this component
	 * @param id
	 *            The component id
	 * @param fragment
	 *            The associated markup
	 */
	public WicketHeadContainer(final MarkupContainer parent, final String id,
			final MarkupFragment fragment)
	{
		super(parent, id);

		setRenderBodyOnly(true);
		
		this.fragment = fragment;
		String namespace = this.fragment.getMarkup().getWicketNamespace();
		this.scope = this.fragment.getTag().getAttributes().getString(namespace + ":scope");
	}

	/**
	 * 
	 * @see wicket.Component#getMarkupFragment()
	 */
	@Override
	public MarkupFragment getMarkupFragment()
	{
		return this.fragment;
	}

	/**
	 * Enable/disable the header part and its childs. It is very similar to
	 * setVisible() but as child components are added to the parent (Panel,
	 * Border) and not the WicketHeadContainer, setVisible() does not work.
	 * Remember that WicketHeadContainer is transparent and usually it will not
	 * have any child components.
	 * 
	 * @param enable
	 */
	public final void setEnable(final boolean enable)
	{
		this.enable = enable;
	}

	/**
	 * @return True, if header part and it childs are to be rendered.
	 */
	public final boolean isEnable()
	{
		return this.enable;
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
	 * @see wicket.MarkupContainer#isTransparentResolver()
	 */
	@Override
	public boolean isTransparentResolver()
	{
		return true;
	}

	/**
	 * @see wicket.MarkupContainer#onRender(wicket.markup.MarkupStream)
	 */
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		// Remember the current Response and make sure it'll be restored no
		// matter what happens during render
		Response response = getRequestCycle().getResponse();
		try
		{
			// If not visible (== not enabled) than render wicket:head and all
			// its child components but write the response into a Null response.
			// Nothing will be forwarded to the client.
			if (this.enable == false)
			{
				getRequestCycle().setResponse(NullResponse.getInstance());
			}

			// Render <wicket:head> with the markup fragment assigned
			super.onRender(new MarkupStream(getMarkupFragment()));
		}
		finally
		{
			// make sure the markup stream is updated
			markupStream.skipComponent();

			// restore the orginial respone object
			getRequestCycle().setResponse(response);
		}
	}
}