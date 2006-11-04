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

	private boolean enable = true;

	/**
	 * @param parent
	 *            The owner parent of this component
	 * @param id
	 *            The component id
	 * @param fragment
	 *            The markup fragment associated with the wicket:head
	 */
	public WicketHeadContainer(final MarkupContainer parent, final String id,
			final MarkupFragment fragment)
	{
		super(parent, id);

		// It is an auto component; make sure the markup loads properly
		getMarkupFragment();
		
		setRenderBodyOnly(true);
	}

	/**
	 * Enable/disable the header part and its childs. It is very similar to
	 * setVisible() but as childs are added to the parent (Panel, Border) and
	 * not the HeaderContainer, does setVisible() not work.
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
		if (this.scope == null)
		{
			String namespace = getMarkupFragment().getMarkup().getWicketNamespace();
			this.scope = getMarkupFragment().getTag().getAttributes().getString(namespace + ":scope");
		}

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
		Response response = getRequestCycle().getResponse();
		try
		{
			if (this.enable == false)
			{
				getRequestCycle().setResponse(NullResponse.getInstance());
			}
			super.onRender(new MarkupStream(getMarkupFragment()));
		}
		finally
		{
			markupStream.skipComponent();
			getRequestCycle().setResponse(response);
		}
	}
}