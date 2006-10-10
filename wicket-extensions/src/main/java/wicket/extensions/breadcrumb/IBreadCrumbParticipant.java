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
package wicket.extensions.breadcrumb;

import java.io.Serializable;

import wicket.Component;
import wicket.extensions.breadcrumb.panel.BreadCrumbPanel;

/**
 * Bread crumb participants function as proxies for components that are part of
 * a bread crumb hierarchy. An example of a bread crumb is:
 * 
 * <pre>
 *     Home &gt; Products &amp; Solutions &gt; Hardware &gt; Desktop Systems
 * </pre>
 * 
 * In a {@link BreadCrumbPanel panel based implementation}, <tt>Home</tt>,
 * <tt>Products &amp; Solutions</tt> etc would be seperate panels that all are
 * bread crumb participants: for instance the <tt>Home</tt> participant's
 * {@link #getTitle() title} would return 'Home', and
 * {@link #getComponent() the component} would be the corresponding panel.
 * 
 * @author Eelco Hillenius
 */
public interface IBreadCrumbParticipant extends Serializable
{
	/**
	 * Gets the participating component. Typically, this is a panel.
	 * 
	 * @return The participating component, must return a non-null value
	 */
	Component getComponent();

	/**
	 * Gets the title of the bread crumb, which will be used for displaying it.
	 * 
	 * @return The title of the bread crumb
	 */
	String getTitle();

	/**
	 * Called when the corresponding bread crumb is activated.
	 * 
	 * @param previous
	 *            The previously active bread crumb participant, possibly null
	 */
	void onActivate(IBreadCrumbParticipant previous);
}
