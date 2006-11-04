/*
 * $Id: WebMarkupContainerWithAssociatedMarkup.java 5861 2006-05-25 20:55:07
 * +0000 (Thu, 25 May 2006) eelco12 $ $Revision$ $Date: 2006-05-25
 * 20:55:07 +0000 (Thu, 25 May 2006) $
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
package wicket.markup.html;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupStream;
import wicket.markup.html.internal.WicketHeadContainer;
import wicket.model.IModel;

/**
 * A WebMarkupContainer, such as Panel or Border, with an associated markup
 * file.
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Juergen Donnerstag
 */
public class WebMarkupContainerWithAssociatedMarkup<T> extends WebMarkupContainer<T>
{
	private static final Log log = LogFactory.getLog(WebMarkupContainerWithAssociatedMarkup.class);

	private static final long serialVersionUID = 1L;

	/** True if body onLoad attribute modifiers have been attached */
	private boolean checkedBody = false;

	/** <wicket:head> is only allowed before <body>, </head>, <wicket:panel> etc. */
	private boolean noMoreWicketHeadTagsAllowed = false;

	/** True, if headers have been added */
	private transient boolean headersInitialized;

	/**
	 * @see Component#Component(MarkupContainer,String)
	 */
	public WebMarkupContainerWithAssociatedMarkup(MarkupContainer parent, final String id)
	{
		this(parent, id, null);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public WebMarkupContainerWithAssociatedMarkup(MarkupContainer parent, final String id,
			IModel<T> model)
	{
		super(parent, id, model);

		getAssociatedMarkup(true);
	}

	/**
	 * Get the child markup fragment with the 'id'
	 * 
	 * @param id
	 * @return MarkupFragment
	 */
	@Override
	public MarkupFragment getMarkupFragment(final String id)
	{
		MarkupFragment fragment = getMarkupFragment().getChildFragment(id, false);
		if (fragment != null)
		{
			return fragment;
		}
		
		return getAssociatedMarkup(true).getChildFragment(id, true);
	}

	/**
	 * @see wicket.Component#renderHead(wicket.markup.html.IHeaderResponse)
	 */
	@Override
	public void renderHead(final IHeaderResponse response)
	{
		if (isVisible())
		{
			// Load the markup and create the header containers if necessary
			getAssociatedMarkup(true);
			
			for (Component child : this)
			{
				if (child instanceof WicketHeadContainer)
				{
					child.render(new MarkupStream(child.getMarkupFragment()));
				}
			}
			
			super.renderHead(response);
		}
	}
}