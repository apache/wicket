/*
 * $Id$ $Revision$
 * $Date$
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

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import wicket.Application;
import wicket.IRequestCycleFactory;
import wicket.Session;

/**
 * A session subclass for the HTTP protocol. 
 * 
 * @author Jonathan Locke
 */
public class WebSession extends Session implements HttpSessionBindingListener
{
	/** log. careful, this log is used to trigger profiling too! */
	// private static Log log = LogFactory.getLog(WebSession.class);

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
	protected WebSession(final WebApplication application)
	{
		super(application);
	}

	/**
	 * Invalidates this session at the end of the current request. If you need
	 * to invalidate the session immediately, you can do this by calling
	 * invalidateNow(), however this will remove all Wicket components from this
	 * session, which means that you will no longer be able to work with them.
	 */
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
		getSessionStore().invalidate();
	}

	/**
	 * Any attach logic for session subclasses.
	 */
	protected void attach()
	{
	}

	/**
	 * @see wicket.Session#detach()
	 */
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
	protected IRequestCycleFactory getRequestCycleFactory()
	{
		if (requestCycleFactory == null)
		{
			this.requestCycleFactory = ((WebApplication)getApplication())
					.getDefaultRequestCycleFactory();
		}

		return this.requestCycleFactory;
	}

	/**
	 * Updates the session, e.g. for replication purposes.
	 */
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
	final void initForRequest()
	{
		// Set the current session
		set(this);

		attach();
	}

	/**
	 * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
	 */
	public void valueBound(HttpSessionBindingEvent event)
	{
	}

	/**
	 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
	 */
	public void valueUnbound(HttpSessionBindingEvent event)
	{
		// will happen when the session gets invalidated or a timeout.
		String id = event.getSession().getId();
		Application application = getApplication();
		if(application instanceof WebApplication)
		{
			((WebApplication)application).sessionDestroyed(id);
		}
		else
		{
			// couldn't clean up the sessions because application not found for this session.
			// TODO we could try to get it through the servletcontext, but how to get the context key?
		}
	}
}
