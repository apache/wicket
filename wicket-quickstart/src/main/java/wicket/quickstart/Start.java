/*
 * $Id$
 * $Revision$
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
package wicket.quickstart;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;

/**
 * Seperate startup class for people that want to run the examples directly.
 */
public class Start
{
	/**
	 * Used for logging.
	 */
	private static Log log = LogFactory.getLog(Start.class);

	/**
	 * Construct.
	 */
	Start()
	{
		super();
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
}
