/*
 * $Id: PanelCachingTab.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25 May
 * 2006) $
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

import wicket.MarkupContainer;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * A simple decorator that will cache the panel returned from the first call to
 * getPanel()
 * 
 * @see wicket.extensions.markup.html.tabs.ITab
 * @see wicket.extensions.markup.html.tabs.TabbedPanel
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class PanelCachingTab implements ITab
{
	private static final long serialVersionUID = 1L;

	private Panel panel;
	private ITab delegate;

	/**
	 * Constructor
	 * 
	 * @param delegate
	 *            ITab implementation to decorate
	 */
	public PanelCachingTab(ITab delegate)
	{
		this.delegate = delegate;
	}

	/**
	 * @see wicket.extensions.markup.html.tabs.ITab#getTitle()
	 */
	public IModel getTitle()
	{
		return delegate.getTitle();
	}

	/**
	 * @see wicket.extensions.markup.html.tabs.ITab#getPanel(MarkupContainer, java.lang.String)
	 */
	public Panel getPanel(MarkupContainer parent, final String panelId)
	{
		if (panel == null)
		{
			panel = delegate.getPanel(parent, panelId);
		}
		return panel;
	}
}