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
package wicket.examples.cdapp;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import wicket.Response;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebSession;

/**
 * Special request cycle for this application that opens and closes a hibernate session
 * for each request.
 */
public final class CdAppRequestCycle extends WebRequestCycle
{
	/** the Hibernate session factory. */
	private final SessionFactory sessionFactory;

	/** the current hibernate session. */
	private Session session = null;

	/**
	 * Construct.
	 * @param application application object
	 * @param session session object
	 * @param request request object
	 * @param response response object
	 * @param sessionFactory hibernate session factory
	 */
	public CdAppRequestCycle(WebApplication application, WebSession session,
			WebRequest request, Response response, SessionFactory sessionFactory)
	{
		super(application, session, request, response);
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see wicket.RequestCycle#onBeginRequest()
	 */
	protected void onBeginRequest()
	{
		try
		{
			session = sessionFactory.openSession();
		}
		catch (HibernateException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see wicket.RequestCycle#onEndRequest()
	 */
	protected void onEndRequest()
	{
		try
		{
			session.close();
		}
		catch (HibernateException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			session = null;
		}
	}

	/**
	 * Gets the hibernate session for this request.
	 * @return the session
	 */
	public Session getHibernateSession()
	{
		return session;
	}
}