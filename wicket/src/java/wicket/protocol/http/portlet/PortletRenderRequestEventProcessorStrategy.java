/*
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
package wicket.protocol.http.portlet;

import wicket.RequestCycle;
import wicket.request.compound.IEventProcessorStrategy;

/**
 * @author Janne Hietam&auml;ki
 * 
 */
public class PortletRenderRequestEventProcessorStrategy implements IEventProcessorStrategy
{

	/*
	 * Process only PortletMode and WindowState changes in the RenderRequests
	 * 
	 * @see wicket.request.compound.IEventProcessorStrategy#processEvents(wicket.RequestCycle)
	 *      @param requestCycle
	 */
	public void processEvents(final RequestCycle requestCycle)
	{
		PortletPage page=(PortletPage)requestCycle.getRequest().getPage();
		if(page!=null)
		{
			PortletRequestCycle cycle = (PortletRequestCycle)requestCycle;
			page.setPortletMode(cycle.getPortletRequest().getPortletRequest().getPortletMode());
			page.setWindowState(cycle.getPortletRequest().getPortletRequest().getWindowState());			
		}
	}

}
