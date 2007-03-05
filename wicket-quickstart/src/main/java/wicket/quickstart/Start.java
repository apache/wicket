/*
 * $Id$ $Revision$ $Date$
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

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Seperate startup class for people that want to run the examples directly.
 */
public class Start
{
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
		Server server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(8080);
		server.setConnectors(new Connector[] {connector});

		WebAppContext web = new WebAppContext();
		web.setContextPath("/quickstart");
		web.setWar("src/webapp");
		server.addHandler(web);

		// Turn this on when you want to use JMX
		// MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		// MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
		// server.getContainer().addEventListener(mBeanContainer);
		// mBeanContainer.start();

		try
		{
			server.start();
			System.out.println(">>> PRESS ANY KEY TO SHUTDOWN THE SERVER");
			while (System.in.available() == 0)
			{
				Thread.sleep(2000);
			}
			System.out.println(">>> SHUTTING DOWN SERVER");
			server.stop();
			server.join();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(100);
		}
	}
}
