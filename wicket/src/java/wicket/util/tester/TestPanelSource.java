/*
 * $Id: TestPanelSource.java 3028 2005-10-20 19:35:16 +0000 (Thu, 20 Oct 2005)
 * jdonnerstag $ $Revision$ $Date: 2005-10-20 19:35:16 +0000 (Thu, 20 Oct
 * 2005) $
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
package wicket.util.tester;

import java.io.Serializable;

import wicket.markup.html.panel.Panel;

/**
 * A test panel factory for WicketTester
 * 
 * @author Ingram Chen
 */
public interface TestPanelSource extends Serializable
{
	/**
	 * Define a panel instance source for WicketTester
	 * 
	 * @param panelId
	 *            panelId of the testing panel
	 * @return Panel testing panel instance, note that testing panel's
	 *         componentId must use supplied <code>panelId</code>.
	 */
	public Panel getTestPanel(final String panelId);
}
