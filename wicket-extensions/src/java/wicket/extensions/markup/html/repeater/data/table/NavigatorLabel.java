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
package wicket.extensions.markup.html.repeater.data.table;


import wicket.Component;
import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;


/**
 * Label that provides Showing x to y of z message given a dataview.
 * 
 * @author igor
 * 
 */
public class NavigatorLabel extends Label
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 *            component id
	 * @param dataView
	 *            dataview
	 */
	public NavigatorLabel(final String id, final DataView dataView)
	{
		super(id, new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component component)
			{
				int of = dataView.getItemCount();
				int from = dataView.getCurrentPage() * dataView.getItemsPerPage();
				int to = Math.min(of, from + dataView.getItemsPerPage());

				from++;

				if (of == 0)
				{
					from = 0;
					to = 0;
				}

				return new String("Showing " + from + " to " + to + " of " + of);
			}
		});
	}
}
