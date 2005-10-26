/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.repeater.data.sort;

import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.markup.html.border.Border;

/**
 * A component that wraps markup with an OrderByLink. This has the advantage of
 * being able to add the attribute modifier to the wrapping element as opposed
 * to the link, so that it can be attached to &lt;th&gt; or any other element.
 * 
 * For example:
 * 
 * &lt;th wicket:id="order-by-border"&gt;Heading&lt;/th&gt;
 * 
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * 
 */
public class OrderByBorder extends Border
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 *            component id
	 * @param sortProperty
	 *            sort propert this link is responsible for
	 * @param dataView
	 *            data view whose sorting dataprovider will be updated by this
	 *            link
	 * @param cssProvider
	 *            implementation of DataView.ICssProvider
	 */
	public OrderByBorder(String id, String sortProperty, DataView dataView,
			OrderByLink.ICssProvider cssProvider)
	{
		super(id);
		OrderByLink link = new OrderByLink("orderByLink", sortProperty, dataView,
				OrderByLink.VoidCssProvider.getInstance());
		add(link);
		add(new OrderByLink.CssModifier(link, cssProvider));
	}

	/**
	 * @param id
	 *            component id
	 * @param sortProperty
	 *            sort propert this link is responsible for
	 * @param dataView
	 *            data view whose sorting dataprovider will be updated by this
	 *            link
	 */
	public OrderByBorder(String id, String sortProperty, DataView dataView)
	{
		this(id, sortProperty, dataView, OrderByLink.DefaultCssProvider.getInstance());
	}

}
