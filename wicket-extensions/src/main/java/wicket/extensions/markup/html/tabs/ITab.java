/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:54:31 +0200 (vr, 26 mei 2006) $
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
package wicket.extensions.markup.html.tabs;

import java.io.Serializable;

import wicket.MarkupContainer;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * Interface used to represent a single tab in a TabbedPanel
 * 
 * @see wicket.extensions.markup.html.tabs.TabbedPanel
 * @see wicket.extensions.markup.html.tabs.AbstractTab
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface ITab extends Serializable
{
	/**
	 * @return IModel used to represent the title of the tab. Must contain a
	 *         string.
	 */
	IModel getTitle();

	/**
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param panelId
	 *            returned panel MUST have this id
	 * @return a Panel object that will be placed as the content panel
	 */
	Panel getPanel(MarkupContainer parent, final String panelId);
}