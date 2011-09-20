package org.apache.wicket.wicket4066;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

/**
 * 
 */
public class MySession extends WebSession
{
	private boolean anonymous = true;

	/**
	 * Construct.
	 * 
	 * @param request
	 */
	public MySession(Request request)
	{
		super(request);
	}

	/**
	 * @return {@code false} if the user is authenticated
	 */
	public boolean isAnonymous()
	{
		return anonymous;
	}

	/**
	 * Authenticates the session
	 * 
	 * @param flag
	 *            the authentication flag
	 */
	public void setAnonymous(boolean flag)
	{
		anonymous = flag;
	}
}
