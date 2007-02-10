/*
 * $Id$
 * $Revision$ $Date$
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
package examples.applet.sprockets;

import wicket.protocol.http.WebApplication;
import wicket.settings.IRequestCycleSettings;

/**
 * Sprocket web application demonstrating Swing integration with Wicket.
 * 
 * @author Jonathan Locke
 */
public final class SprocketsApplication extends WebApplication
{
	/**
	 * Constructor.
	 */
	public SprocketsApplication()
	{
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Home.class;
	}
	
	/**
	 * @see wicket.protocol.http.WebApplication#init()
	 */
	@Override
	protected void init()
	{
		getRequestCycleSettings().setRenderStrategy(IRequestCycleSettings.REDIRECT_TO_BUFFER);
	}
}
