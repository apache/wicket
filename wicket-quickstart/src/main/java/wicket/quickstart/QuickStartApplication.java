package wicket.quickstart;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;

import wicket.protocol.http.WebApplication;
import wicket.util.time.Duration;
import wicket.ISessionFactory;
import wicket.Session;

/**
 * Runs the QuickStartApplication when invoked from command line.
 */
public class QuickStartApplication extends WebApplication
{    
	/** Logging */
	private static final Log log = LogFactory.getLog(QuickStartApplication.class);

	/**
	 * Main function, starts the jetty server.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
        Server jettyServer = null;
		try
		{
			URL jettyConfig = new URL("file:src/main/resources/jetty-config.xml");
			if (jettyConfig == null)
			{
				log.fatal("Unable to locate jetty-test-config.xml on the classpath");
			}
			jettyServer = new Server(jettyConfig);
			jettyServer.start();
		}
		catch (Exception e)
		{
			log.fatal("Could not start the Jetty server: " + e);
			if (jettyServer != null)
			{
				try
				{
					jettyServer.stop();
				}
				catch (InterruptedException e1)
				{
					log.fatal("Unable to stop the jetty server: " + e1);
				}
			}
		}
	}

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