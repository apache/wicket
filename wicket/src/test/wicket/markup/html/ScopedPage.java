/*
 * $Id: WicketTagComponentResolver.java,v 1.4 2005/01/18 08:04:29 jonathanlocke
 * Exp $ $Revision$ $Date$
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

import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;

/**
 * 
 * @author Juergen Donnerstag
 */
public class ScopedPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private int _clicked = 0;

	/**
	 * Construct.
	 */
	public ScopedPage()
	{
		super();
		new Label(this, "unscoped", "unscoped");

		new ScopedLabel(this, "clicked", new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Object getObject()
			{
				return "Clicked: " + _clicked;
			}
		});

		new ScopedLabel(this, "global", "Global");

		new ScopedLink(this, "globalLink")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				_clicked++;
			}
		};

		WebMarkupContainer cont1 = new WebMarkupContainer(this, "cont1");
		new ScopedLabel(cont1, "localscoped", "Local Scoped");
		new Label(cont1, "local", "Local");

		WebMarkupContainer cont11 = new WebMarkupContainer(cont1, "cont11");
		new Label(cont11, "global", " hide global");

		WebMarkupContainer cont2 = new WebMarkupContainer(this, "cont2");
		new Label(cont2, "local", "Local2");
		new ScopedLabel(cont2, "localscoped", "Local Scoped");
	}
}
