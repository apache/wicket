/*
 * Created on Dec 2, 2004
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package wicket.quickstart;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;

import wicket.protocol.http.WebApplication;
import wicket.util.time.Duration;
import wicket.ApplicationSettings;

/**
 */
public class QuickStart extends WebApplication
{
	/**
	 * Used for logging.
	 */
	private static final Log log = LogFactory.getLog(QuickStart.class);

	/**
	 * The Jetty server.
	 */
	private static Server jettyServer;

	/**
	 * Main function, starts the jetty server.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
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

	public QuickStart()
	{
		ApplicationSettings settings = getSettings();
		getPages().setHomePage(Index.class);
		if (!Boolean.getBoolean("cache-templates"))
		{
			Duration pollFreq = Duration.ONE_SECOND;
			settings.setResourcePollFrequency(pollFreq);
			log.info("template caching is INACTIVE");
		}
		else
		{
			log.info("template caching is ACTIVE");
		}
	}
}