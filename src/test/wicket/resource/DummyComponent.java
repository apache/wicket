/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.resource;

import wicket.Component;
import wicket.IApplication;
import wicket.RequestCycle;
import wicket.Session;

/**
 * Dummy component used for testing or resource loading funationality.
 * @author Chris Turner
 */
public class DummyComponent extends Component
{

	// Customised session for testing
	private Session testSession;

	/**
	 * Create the component with the given name.
	 * @param name The name of the component
	 * @param application The application for this component
	 */
	public DummyComponent(final String name, final IApplication application)
	{
		super(name);
		testSession = new Session(application)
		{
            public void invalidate()
            {
            }
		};
	}

	/**
	 * Implementation which renders this component.
	 * @param cycle The response to write to
	 */
	protected void handleRender(RequestCycle cycle)
	{
	}

	/**
	 * Override the session provider for testing purposes.
	 * @return The test session
	 */
	public Session getSession()
	{
		return testSession;
	}

}

// 
