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
package wicket.examples;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;

import wicket.protocol.http.WebApplication;
import wicket.util.crypt.NoCrypt;

/**
 * WicketServlet class for hello world example.
 * @author Jonathan Locke
 */
public abstract class WicketExampleApplication extends WebApplication
{
	/**
	 * Used for logging.
	 */
	private static Log log = LogFactory.getLog(WicketExampleApplication.class);
	
    /**
     * Constructor.
     */
    public WicketExampleApplication()
    {        
        // WARNING: DO NOT do this on a real world application unless
        // you really want your app's passwords all passed around and 
        // stored in unencrypted browser cookies (BAD IDEA!)!!! 
        
        // The NoCrypt class is being used here because not everyone
        // has the java security classes required by Crypt installed
        // and we want them to be able to run the examples out of the
        // box.
        getSettings().setCryptClass(NoCrypt.class);
    }
    
    /**
     * Determine operations mode: deployment or development
     */
	protected void init()
	{
	    if (this.getWicketServlet().getServletContext().getInitParameter("deployment") != null)
	    {
	    	// Use deployment settings
	        getSettings().configure("deployment");
	    }
	    else
	    {
	        // Use development settings 
	        getSettings().configure("development", "src/java");
	    }
	}
    
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
			URL jettyConfig = new URL("file:src/etc/jetty-config.xml");
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
}
