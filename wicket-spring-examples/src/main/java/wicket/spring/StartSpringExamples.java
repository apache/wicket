/*
 * $Id: StartSpringExamples.java 3482 2005-12-23 23:04:02 +0000 (Fri, 23 Dec 2005) ivaynberg $
 * $Revision: 3482 $ $Date: 2005-12-23 23:04:02 +0000 (Fri, 23 Dec 2005) $
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
package wicket.spring;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.mortbay.jetty.Server;

/**
 * Seperate startup class for people that want to run the examples directly.
 */
public class StartSpringExamples
{
	/**
	 * Used for logging.
	 */
	private static final Log log = LogFactory.getLog(StartSpringExamples.class);

	/**
	 * Construct.
	 */
	StartSpringExamples()
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
        BasicConfigurator.configure();
        Server jettyServer = null;
		try
		{
			URL jettyConfig = new URL("file:src/main/java/jetty-config.xml");
			if (jettyConfig == null)
			{
				log.fatal("Unable to locate jetty-config.xml on the classpath");
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
