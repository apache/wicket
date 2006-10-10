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
package wicket.extensions.breadcrumb.panel;

import java.io.Serializable;

import wicket.extensions.breadcrumb.IBreadCrumbModel;

/**
 * Factory interface to enabled defered creation of a bread crumb panel while
 * getting the proper id for creation. Mainly meant for supporting
 * {@link BreadCrumbPanel#activate(IBreadCrumbPanelFactory)}.
 */
public interface IBreadCrumbPanelFactory extends Serializable
{
	/**
	 * Creates a new {@link BreadCrumbPanel bread crumb panel} instance. The
	 * provided component id must be used when creating the panel.
	 * 
	 * @param componentId
	 *            The component id for the new panel.
	 * @param breadCrumbModel
	 *            The bread crumb model
	 * @return A new bread crumb panel instance
	 */
	BreadCrumbPanel create(String componentId, IBreadCrumbModel breadCrumbModel);
}