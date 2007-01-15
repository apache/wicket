package wicket.quickstart;

import wicket.Request;
import wicket.Session;
import wicket.protocol.http.WebApplication;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see wicket.quickstart.Start#main(String[])
 */
public class QuickStartApplication extends WebApplication
{
	/**
	 * Constructor
	 */
	public QuickStartApplication()
	{
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class<Index> getHomePage()
	{
		return Index.class;
	}

	/**
	 * @see wicket.protocol.http.WebApplication#newSession(wicket.Request)
	 */
	public Session newSession(Request request)
	{
		return new QuickStartSession(QuickStartApplication.this, request);
	}

}