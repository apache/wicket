/*
 * $Id: JettyDecorator.java 461192 2006-06-28 08:37:16 +0200 (Wed, 28 Jun 2006)
 * ehillenius $ $Revision: 464023 $ $Date: 2006-06-28 08:37:16 +0200 (Wed, 28 Jun
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

import junit.framework.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * JUnit decorator for starting and stopping a local instance Jetty for usage
 * with test cases.
 * <p>
 * It uses property 'jettyConfig' to load the - mandatory - Jetty configuration
 * document. Eg '/jetty-test-config.xml' is loaded from the classpath root (so
 * providing the configuration document in the root of the test classes folder
 * will suffice) but 'file://c:/mydir/myconfig.xml' should work as well. If
 * property 'jettyConfig' is not provided (default == null), the properties
 * 'port', 'webappContextRoot' and 'contextPath' are used to start a Jetty
 * instance.
 * </p>
 * <p>
 * Property useJettyPlus (default == false) is used to decide whether JettyPlus
 * should be used, or just the basic version of Jetty. JettyPlus provides
 * support for JNDI, datasources, transactions etc.
 * </p>
 * <p>
 * Usage:
 * 
 * <pre>
 *           
 *            ...
 *              public static Test suite() 
 *              {
 *           	    TestSuite suite = new TestSuite();
 *           	    suite.addTest(new JettyDecoratorWithArgsTest(&quot;testPing&quot;));
 *           	    JettyDecorator deco = new JettyDecorator(suite);
 *           	    deco.setPort(8098);
 *           	    deco.setWebappContextRoot(&quot;src/webapp&quot;);
 *           	    deco.setContextPath(&quot;/test&quot;);
 *           	    deco.setUseJettyPlus(false);
 *           	    return deco;
 *              }
 *            ...
 *            
 * </pre>
 * 
 * Jetty will be started before the tests are actually run, and will be stopped
 * afterwards.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class JettyDecorator extends AbstractJettyDecorator
{
	/** logger. */
	private static final Logger log = LoggerFactory.getLogger(JettyDecorator.class);

	/** instance of jetty server. */
	private Server server = null;

	/**
	 * construct with test.
	 * 
	 * @param test
	 *            test case
	 */
	public JettyDecorator(final Test test)
	{
		super(test);
	}

	/**
	 * Start Jetty.
	 * 
	 * @throws Exception
	 * @see junit.extensions.TestSetup#setUp()
	 */
	public void setUp() throws Exception
	{
		server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(getPort());
		server.setConnectors(new Connector[] { connector });

		WebAppContext web = new WebAppContext();
		web.setContextPath(getContextPath());
		web.setWar(getWebappContextRoot());
		server.addHandler(web);

		log.info("Starting Jetty");
		server.start();
		log.info("Jetty started");
	}

	/**
	 * Stop Jetty.
	 * 
	 * @see junit.extensions.TestSetup#tearDown()
	 */
	public void tearDown()
	{
		log.info("Stopping Jetty");
		try
		{
			server.stop();
			log.info("Jetty stopped");
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
		}
	}
}