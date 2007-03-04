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

import junit.framework.Test;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	@Override
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
	@Override
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