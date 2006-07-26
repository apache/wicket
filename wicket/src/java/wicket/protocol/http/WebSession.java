/*
 * $Id: WebSession.java 5064 2006-03-21 11:30:05 -0800 (Tue, 21 Mar 2006)
 * jonathanlocke $ $Revision$ $Date: 2006-03-21 11:30:05 -0800 (Tue, 21
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
package wicket.protocol.http;

import wicket.Application;
import wicket.IRequestCycleFactory;
import wicket.RequestCycle;
import wicket.Session;
import wicket.protocol.http.request.WebClientInfo;
import wicket.request.ClientInfo;

/**
 * A session subclass for the HTTP protocol.
 * 
 * @author Jonathan Locke
 */
public class WebSession extends Session
{
	/** log. careful, this log is used to trigger profiling too! */
	// private static final Log log = LogFactory.getLog(WebSession.class);
	private static final long serialVersionUID = 1L;

	/** The request cycle factory for the session */
	private transient IRequestCycleFactory requestCycleFactory;

	/** True, if session has been invalidated */
	private transient boolean sessionInvalidated = false;

	/**
	 * Constructor
	 * 
	 * @param application
	 *            The application
	 */
	public WebSession(final Application application)
	{
		super(application);
	}

	/**
	 * Invalidates this session at the end of the current request. If you need
	 * to invalidate the session immediately, you can do this by calling
	 * invalidateNow(), however this will remove all Wicket components from this
	 * session, which means that you will no longer be able to work with them.
	 */
	@Override
	public void invalidate()
	{
		sessionInvalidated = true;
	}

	/**
	 * Invalidates this session immediately. Calling this method will remove all
	 * Wicket components from this session, which means that you will no longer
	 * be able to work with them.
	 */
	public void invalidateNow()
	{
		getSessionStore().invalidate(RequestCycle.get().getRequest());
	}

	/**
	 * Any attach logic for session subclasses.
	 */
	protected void attach()
	{
	}

	/**
	 * Called on the end of handling a request, when the RequestCycle is about
	 * to be detached from the current thread.
	 * 
	 * @see wicket.Session#detach()
	 */
	@Override
	protected void detach()
	{
		if (sessionInvalidated)
		{
			invalidateNow();
		}
	}

	/**
	 * @see wicket.Session#getRequestCycleFactory()
	 */
	@Override
	protected IRequestCycleFactory getRequestCycleFactory()
	{
		if (requestCycleFactory == null)
		{
			this.requestCycleFactory = ((WebApplication)getApplication()).getRequestCycleFactory();
		}

		return this.requestCycleFactory;
	}

	/**
	 * Updates the session, e.g. for replication purposes.
	 */
	@Override
	protected void update()
	{
		if (sessionInvalidated == false)
		{
			super.update();
		}
	}

	/**
	 * Initializes this session for a request.
	 */
	public final void initForRequest()
	{
		// Set the current session
		set(this);

		attach();
	}

	/**
	 * @see wicket.Session#getClientInfo()
	 */
	@Override
	public WebClientInfo getClientInfo()
	{
		return (WebClientInfo)super.getClientInfo();
	}
}
