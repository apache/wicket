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

import wicket.Application;
import wicket.IRequestCycleFactory;
import wicket.protocol.http.WebSession;
import wicket.session.ISessionStore;

/**
 * A session subclass for the PortletSession
 * 
 * @author Janne Hietam&auml;ki
 * 
 */
public class WicketPortletSession extends WebSession
{
	private static final long serialVersionUID = 1L;

	/** The request cycle factory for the session */
	private transient IRequestCycleFactory requestCycleFactory;

	/**
	 * Constructor
	 * 
	 * @param application
	 *            The application
	 */
	protected WicketPortletSession(PortletApplication application)
	{
		super(application);
	}

	/**
	 * @see wicket.Session#getRequestCycleFactory()
	 */
	protected IRequestCycleFactory getRequestCycleFactory()
	{
		if (requestCycleFactory == null)
		{
			this.requestCycleFactory = ((PortletApplication)Application.get())
			.getDefaultRequestCycleFactory();
		}

		return this.requestCycleFactory;
	}


	/**
	 * Gets the session store.
	 * 
	 * @return the session store
	 */
	protected final ISessionStore getSessionStore()
	{
		return getApplication().getSessionStore(); 
	}
}