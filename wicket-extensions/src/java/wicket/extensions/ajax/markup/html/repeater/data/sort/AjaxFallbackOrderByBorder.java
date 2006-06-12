/*
 * $Id: OrderByBorder.java 3345 2005-12-05 08:29:28 +0000 (Mon, 05 Dec 2005)
 * ivaynberg $ $Revision: 3345 $ $Date: 2005-12-05 08:29:28 +0000 (Mon, 05 Dec
 * 2005) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.ajax.markup.html.repeater.data.sort;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.border.Border;


/**
 * Ajaxified version of {@link OrderByBorder}
 * 
 * @see OrderByBorder
 * 
 * @since 1.2.1
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxFallbackOrderByBorder extends Border
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 */
	public AjaxFallbackOrderByBorder(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator)
	{
		this(parent, id, property, stateLocator, AjaxFallbackOrderByLink.DefaultCssProvider
				.getInstance(), null);
	}


	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param cssProvider
	 */
	public AjaxFallbackOrderByBorder(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator, AjaxFallbackOrderByLink.ICssProvider cssProvider)
	{
		this(parent, id, property, stateLocator, cssProvider, null);
	}


	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param decorator
	 */
	public AjaxFallbackOrderByBorder(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator, IAjaxCallDecorator decorator)
	{
		this(parent, id, property, stateLocator, AjaxFallbackOrderByLink.DefaultCssProvider
				.getInstance(), decorator);
	}


	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param cssProvider
	 * @param decorator
	 */
	public AjaxFallbackOrderByBorder(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator, AjaxFallbackOrderByLink.ICssProvider cssProvider,
			final IAjaxCallDecorator decorator)
	{
		super(parent, id);
		AjaxFallbackOrderByLink link = new AjaxFallbackOrderByLink(this, "orderByLink", property,
				stateLocator, cssProvider, decorator)
		{

			private static final long serialVersionUID = 1L;

			protected void onSortChanged()
			{
				AjaxFallbackOrderByBorder.this.onSortChanged();
			}

			protected void onAjaxClick(AjaxRequestTarget target)
			{
				AjaxFallbackOrderByBorder.this.onAjaxClick(target);

			}
		};
		add(new AjaxFallbackOrderByLink.CssModifier(link, cssProvider));
	}

	
	/**
	 * This method is a hook for subclasses to perform an action after sort has
	 * changed
	 */
	protected void onSortChanged()
	{
		// noop
	}

	protected abstract void onAjaxClick(AjaxRequestTarget target);


}
