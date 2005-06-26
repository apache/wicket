/*
 * $Id$
 * $Revision$
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
package wicket.extensions.markup.html.navmenu.style.tabs;

import wicket.extensions.markup.html.navmenu.MenuModel;
import wicket.extensions.markup.html.navmenu.MenuRow;
import wicket.extensions.markup.html.navmenu.MenuRowModel;
import wicket.markup.html.panel.Panel;

/**
 * Two-level navigation menu with a tabs style.
 *
 * @author Eelco Hillenius
 */
public final class TabsNavigationMenu extends Panel
{
	/** the model. */
	private final MenuModel menuModel;

	/**
	 * Construct.
	 * @param id component id
	 * @param model the model
	 */
	public TabsNavigationMenu(String id, MenuModel model)
	{
		super(id);
		this.menuModel = model;
		MenuRow level0 = new MenuRow("level0", new MenuRowModel(model, 0), TabsStyle.LEVEL_0);
		MenuRow level1 = new MenuRow("level1", new MenuRowModel(model, 1), TabsStyle.LEVEL_1);
		add(level0);
		add(level1);
	}
}
