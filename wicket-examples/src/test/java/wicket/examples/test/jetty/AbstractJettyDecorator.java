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
package wicket.examples.test.jetty;

import junit.extensions.TestSetup;
import junit.framework.Test;

/**
 * Base class for Jetty TestDecorators.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractJettyDecorator extends TestSetup
{
	/** context path (webapp name). */
	private String contextPath = "/wicket-examples";

	/** port for http requests. */
	private int port = 8098;

	/** root folder of web application. */
	private String webappContextRoot = "src/main/webapp";

	/**
	 * Construct with test to decorate.
	 * 
	 * @param test
	 *            test to decorate
	 */
	public AbstractJettyDecorator(Test test)
	{
		super(test);
	}

	/**
	 * Get context path (webapp name).
	 * 
	 * @return String Returns the context path (webapp name).
	 */
	public String getContextPath()
	{
		return contextPath;
	}

	/**
	 * Get port for http requests.
	 * 
	 * @return int Returns the port.
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * Get root folder of web application.
	 * 
	 * @return String Returns the webappContextRoot.
	 */
	public String getWebappContextRoot()
	{
		return webappContextRoot;
	}

	/**
	 * Set context path (webapp name).
	 * 
	 * @param contextPath
	 *            context path (webapp name).
	 */
	public void setContextPath(String contextPath)
	{
		this.contextPath = contextPath;
	}

	/**
	 * Set port for http requests.
	 * 
	 * @param port
	 *            port for http requests.
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * Set root folder of web application.
	 * 
	 * @param webappContextRoot
	 *            webappContextRoot to set.
	 */
	public void setWebappContextRoot(String webappContextRoot)
	{
		this.webappContextRoot = webappContextRoot;
	}
}