package wicket.quickstart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.ISessionFactory;
import wicket.Session;
import wicket.protocol.http.WebApplication;
import wicket.util.time.Duration;

/**
 * Runs the QuickStartApplication when invoked from command line.
 */
public class QuickStartApplication extends WebApplication
{    
	/** Logging */
	private static final Log log = LogFactory.getLog(QuickStartApplication.class);

    /**
     * Constructor
     */
	public QuickStartApplication()
	{
		getPages().setHomePage(Index.class);
		if (!Boolean.getBoolean("cache-markup"))
		{
			getSettings().setResourcePollFrequency(Duration.ONE_SECOND);
			log.info("Markup caching is INACTIVE");
		}
		else
		{
			log.info("Markup caching is ACTIVE");
		}
	}

    /**
     * @see wicket.protocol.http.WebApplication#getSessionFactory()
     */
    public ISessionFactory getSessionFactory()
    {
        return new ISessionFactory()
        {        	
			public Session newSession()
            {
                return new QuickStartSession(QuickStartApplication.this);
            }
        };
    }
}