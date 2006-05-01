/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ====================================================================
 * Copyright (c) 2003, Open Edge B.V. All rights reserved. Redistribution and
 * use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: Redistributions of source
 * code must retain the above copyright notice, this list of conditions and the
 * following disclaimer. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither
 * the name of OpenEdge B.V. nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package nl.openedge.util.jetty;

import junit.extensions.TestSetup;
import junit.framework.Test;

/**
 * Base class for Jetty TestDecorators.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractJettyDecorator extends TestSetup
{
	/** default http listen port. */
	private static final int DEFAULT_PORT = 8080;

	/**
	 * Whether to use JettyPlus; if true, org.mortbay.jetty.plus.Server will be
	 * instantiated, if false, org.mortbay.jetty.Server will be instantiated.
	 */
	private boolean useJettyPlus = false;

	/** URL of jetty configuration document. */
	private String jettyConfig;

	/** root folder of web application. */
	private String webappContextRoot = "/.";

	/** port for http requests. */
	private int port = DEFAULT_PORT;

	/** context path (webapp name). */
	private String contextPath = "/";

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
	 * Get jettyConfig; URL of jetty configuration document.
	 * 
	 * @return String Returns the URL of jetty configuration document.
	 */
	public String getJettyConfig()
	{
		return jettyConfig;
	}

	/**
	 * Set jettyConfig; URL of jetty configuration document.
	 * 
	 * @param newJettyConfig
	 *            URL of jetty configuration document.
	 * @param classCallee
	 *            calling class for getting config resource
	 */
	public void setJettyConfig(String newJettyConfig, Class classCallee)
	{
		jettyConfig = newJettyConfig;
	}

	/**
	 * Get whether to use JettyPlus; if true, org.mortbay.jetty.plus.Server will
	 * be instantiated, if false, org.mortbay.jetty.plus.Server will be
	 * instantiated.
	 * 
	 * @return boolean whether to use JettyPlus.
	 */
	public boolean isUseJettyPlus()
	{
		return useJettyPlus;
	}

	/**
	 * Set whether to use JettyPlus; if true, org.mortbay.jetty.plus.Server will
	 * be instantiated, if false, org.mortbay.jetty.plus.Server will be
	 * instantiated.
	 * 
	 * @param useJettyPlus
	 *            whether to use JettyPlus.
	 */
	public void setUseJettyPlus(boolean useJettyPlus)
	{
		this.useJettyPlus = useJettyPlus;
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
	 * Get root folder of web application.
	 * 
	 * @return String Returns the webappContextRoot.
	 */
	public String getWebappContextRoot()
	{
		return webappContextRoot;
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
	 * Set context path (webapp name).
	 * 
	 * @param contextPath
	 *            context path (webapp name).
	 */
	public void setContextPath(String contextPath)
	{
		this.contextPath = contextPath;
	}
}