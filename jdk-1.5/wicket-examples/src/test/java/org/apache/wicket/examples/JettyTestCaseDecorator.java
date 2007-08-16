/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.examples;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Test decorator that starts a jetty instance
 * 
 * @author ivaynberg
 * 
 */
public class JettyTestCaseDecorator extends TestSetup
{

	private Server server;
	private String contextPath;
	private String webappLocation;
	
	/**
	 * @param test
	 */
	public JettyTestCaseDecorator(Test test)
	{
		super(test);
	}

	protected void setUp() throws Exception
	{
		server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(8098);
		server.setConnectors(new Connector[] { connector });

		WebAppContext web = new WebAppContext();
		if (contextPath == null)
			web.setContextPath("/wicket-examples");
		else
			web.setContextPath(contextPath);

		if (webappLocation == null)
		{
			String basedir = System.getProperty("basedir");
			String path = "";
			if (basedir != null)
				path = basedir + "/";
			path += "src/main/webapp";

			web.setWar(path);
		}
		else
		{
			web.setWar(webappLocation);
		}
		server.addHandler(web);

		server.start();

		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
		server.stop();
		server.join();
	}

	public String getContextPath()
	{
		return contextPath;
	}

	public void setContextPath(String contextPath)
	{
		this.contextPath = contextPath;
	}

	public String getWebappLocation()
	{
		return webappLocation;
	}

	public void setWebappLocation(String webappLocation)
	{
		this.webappLocation = webappLocation;
	}

}
