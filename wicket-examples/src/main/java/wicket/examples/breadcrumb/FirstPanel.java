/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.examples.breadcrumb;

import wicket.MarkupContainer;
import wicket.extensions.breadcrumb.IBreadCrumbModel;
import wicket.extensions.breadcrumb.panel.BreadCrumbPanel;
import wicket.extensions.breadcrumb.panel.BreadCrumbPanelLink;

/**
 * Test bread crumb enabled panel.
 * 
 * @author Eelco Hillenius
 */
public class FirstPanel extends BreadCrumbPanel
{
	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 * @param breadCrumbModel
	 */
	public FirstPanel(final MarkupContainer parent, final String id,
			final IBreadCrumbModel breadCrumbModel)
	{
		super(parent, id, breadCrumbModel);
		new BreadCrumbPanelLink(this, "linkToSecond", this, SecondPanel.class);
	}

	/**
	 * @see wicket.extensions.breadcrumb.IBreadCrumbParticipant#getTitle()
	 */
	public String getTitle()
	{
		return "first";
	}
}
