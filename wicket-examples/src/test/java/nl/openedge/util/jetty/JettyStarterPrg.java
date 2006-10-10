/*
 * $Id: JettyStarterPrg.java 4619 2006-02-23 22:25:06 +0000 (Thu, 23 Feb 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-02-23 22:25:06 +0000 (Thu, 23 Feb
 * 2006) $
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.util.MultiException;

/**
 * Program that starts Jetty and an admin monitor.
 * 
 * @author Eelco Hillenius
 */
public class JettyStarterPrg
{
	/**
	 * command line argument for the xml configuration document, value ==
	 * '-xml'.
	 */
	public static final String CMDARG_XML_CONFIG = "-xml";

	/** command line argument for the http listen port, value == '-port'. */
	public static final String CMDARG_PORT = "-port";

	/**
	 * default port for http listen request; used when no port is provided and
	 * no xml doc is used, value == 8080.
	 */
	public static final String DEFAULT_HTTP_LISTEN_PORT = "8080";

	/**
	 * command line argument for the webapp context root folder, value ==
	 * '-webappContextRoot'.
	 */
	public static final String CMDARG_WEBAPP_CONTEXT_ROOT = "-webappContextRoot";

	/**
	 * command line argument for the context path (webapp name), value ==
	 * '-contextPath'.
	 */
	public static final String CMDARG_CONTEXT_PATH = "-contextPath";

	/** command line argument whether to use JettyPlus, value == '-useJettyPlus'. */
	public static final String CMDARG_USE_JETTY_PLUS = "-useJettyPlus";

	/** command line argument for auth key to use, value == '-monitorCommKey'. */
	public static final String CMDARG_MONITOR_COMM_KEY = "-monitorCommKey";

	/** command line argument for monitor port to use, value == '-monitorPort'. */
	public static final String CMDARG_MONITOR_PORT = "-monitorPort";

	/** default stop port. */
	private static final int DEFAULT_STOP_PORT = 8079;

	/** Logger. */
	private static final Log log = LogFactory.getLog(JettyStarterPrg.class);

	/**
	 * Starts Jetty.
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args)
	{
		Properties cmdArguments = new Properties();
		for (int i = 0; i < args.length; i += 2) // put arguments (if any) in
		// cmdArguments
		{
			if (i + 1 < args.length)
			{
				String key = args[i];
				String val = args[i + 1];
				log.info("using arg: " + key + " == " + val);
				cmdArguments.put(key, val);
			} // else: there's an arg without a value
		}

		// get arguments from the previously build properties
		boolean useJettyPlus = Boolean.valueOf(
				cmdArguments.getProperty(CMDARG_USE_JETTY_PLUS, "false")).booleanValue();
		String jettyConfig = cmdArguments.getProperty(CMDARG_XML_CONFIG);
		int port = Integer
				.parseInt(cmdArguments.getProperty(CMDARG_PORT, DEFAULT_HTTP_LISTEN_PORT));
		String webappContextRoot = cmdArguments.getProperty(CMDARG_WEBAPP_CONTEXT_ROOT, "./");
		String contextPath = cmdArguments.getProperty(CMDARG_CONTEXT_PATH, "/");
		String monitorCommKey = cmdArguments.getProperty(CMDARG_MONITOR_COMM_KEY);
		if (monitorCommKey == null)
		{
			monitorCommKey = System.getProperty("STOP.KEY", "mortbay");
		}
		String monitorPortS = cmdArguments.getProperty(CMDARG_MONITOR_PORT);
		int monitorPort;
		if (monitorPortS != null)
		{
			monitorPort = Integer.parseInt(monitorPortS);
		}
		else
		{
			monitorPort = Integer.getInteger("STOP.PORT", DEFAULT_STOP_PORT).intValue();
		}

		try
		{
			// start Jetty
			startServer(jettyConfig, port, webappContextRoot, contextPath, useJettyPlus,
					monitorCommKey, monitorPort);
		}
		catch (MalformedURLException e)
		{
			log.error(e.getMessage(), e);
		}
		catch (MultiException e)
		{
			log.error(e.getMessage(), e);
		}
		catch (JettyHelperException e)
		{
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Start Jetty Server and the admin monitor.
	 * 
	 * @param jettyConfig
	 *            jetty config location; if null, the next three parameters will
	 *            be used instead
	 * @param port
	 *            port for http requests
	 * @param webappContextRoot
	 *            webapplication context root
	 * @param contextPath
	 *            context path (webapp name)
	 * @param useJettyPlus
	 *            whether to use JettyPlus
	 * @param monitorCommKey
	 *            auth key
	 * @param monitorPort
	 *            listen port for admin monitor
	 * @throws MalformedURLException
	 *             when the url is not valid
	 * @throws MultiException
	 *             when Jetty is unable to startup
	 * @throws JettyHelperException
	 *             when the server could not be created
	 */
	private static void startServer(String jettyConfig, int port, String webappContextRoot,
			String contextPath, boolean useJettyPlus, String monitorCommKey, int monitorPort)
			throws MalformedURLException, MultiException, JettyHelperException
	{
		Server jettyServer = null;
		// get instance of proper Jetty server
		if (jettyConfig != null) // either start with xml configuration
		// document
		{
			URL jettyConfigURL = null;
			jettyConfigURL = URLHelper.convertToURL(jettyConfig, JettyStarterPrg.class);
			log.info("Creating Jetty with configuration " + jettyConfigURL);
			jettyServer = JettyHelper.getJettyServerInstance(jettyConfigURL, useJettyPlus);
		}
		else
		// or some basic arguments
		{
			jettyServer = JettyHelper.getJettyServerInstance(port, webappContextRoot, contextPath,
					useJettyPlus);
		}

		// now, before starting the server, first create the monitor
		JettyMonitor monitor = JettyMonitor.startMonitor(jettyServer, monitorCommKey, monitorPort); // start
		// admin
		// monitor
		log.info("Started " + monitor);

		// finally, start the server
		jettyServer.start();
		log.info("Started " + jettyServer);
	}

}