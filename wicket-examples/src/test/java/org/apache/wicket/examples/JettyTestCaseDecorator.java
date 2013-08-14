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

import com.meterware.httpunit.HttpUnitOptions;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Test decorator that starts a jetty instance
 * 
 * @author ivaynberg
 */
public class JettyTestCaseDecorator extends Assert
{
	private Server server;
	private String contextPath;
	private String webappLocation;
	protected int localPort;

	/**
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		HttpUnitOptions.setExceptionsThrownOnScriptError(false);

		System.setProperty("wicket.configuration", "deployment");

		server = new Server(0);
		SelectChannelConnector connector = new SelectChannelConnector();
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
		server.setHandler(web);

		server.start();
		localPort = connector.getLocalPort();
	}

	/**
	 * @throws Exception
	 */
	@After
	public void after() throws Exception
	{
		contextPath = null;
		webappLocation = null;
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
