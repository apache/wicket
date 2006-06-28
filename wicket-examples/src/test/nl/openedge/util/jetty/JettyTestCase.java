/*
 * $Id: JettyTestCase.java 5395 2006-04-16 13:42:28 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 13:42:28 +0000 (Sun, 16 Apr
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;

/**
 * Base class for Jetty test cases. Classes that override this test case will
 * have a local Jetty server started and stopped automatically for each test
 * case. the methods setUp and tearDown are finalized in this class, please use
 * one of the methods 'beforeSetup', 'afterSetup', 'beforeTearDown' and
 * 'afterTearDown'.
 * <p>
 * Method 'beforeSetup' is particularly usefull, as it can be used to configure
 * the Jetty server that is to be created and run. An example of how to do is:
 * <br/>
 * </p>
 * <p>
 * 
 * <pre>
 * public void beforeSetUp()
 * {
 * 	setPort(8098);
 * 	setWebappContextRoot(&quot;src/webapp&quot;);
 * 	setContextPath(&quot;/test&quot;);
 * }
 * </pre>
 * 
 * </p>
 * 
 * @author Eelco Hillenius
 */
public abstract class JettyTestCase extends AbstractJettyTestCase
{
	/** instance of jetty server. */
	private static Server jettyServer = null;

	/** logger. */
	private static final Log log = LogFactory.getLog(JettyDecorator.class);

	/**
	 * Construct.
	 */
	public JettyTestCase()
	{
		super();
	}

	/**
	 * Construct with test case name.
	 * 
	 * @param name
	 *            test case name
	 */
	public JettyTestCase(String name)
	{
		super(name);
	}

	/**
	 * Start Jetty; inhereting classes can override methods beforeSetUp and
	 * afterSetUp for test case specific behavior.
	 * 
	 * @throws Exception
	 */
	@Override
	public final void setUp() throws Exception
	{
		// first let current test case set up fixture
		beforeSetUp();
		// start Jetty
		long begin = System.currentTimeMillis();
		if (getJettyConfig() != null)
		{
			// start Jetty with config document
			jettyServer = JettyHelper.startJetty(getJettyConfig(), isUseJettyPlus());
		}
		else
		{
			// start Jetty with arguments (port etc.)
			jettyServer = JettyHelper.startJetty(getPort(), getWebappContextRoot(),
					getContextPath(), isUseJettyPlus());
		}
		long end = System.currentTimeMillis();
		log.info("Jetty Started (in " + (end - begin) + " milis)");
		// call for further set up
		afterSetUp();
	}

	/**
	 * Stop Jetty; inhereting classes can override methods beforeTearDown and
	 * afterTearDown for test case specific behavior.
	 * 
	 * @throws Exception
	 */
	@Override
	public final void tearDown() throws Exception
	{
		// first let current test case tear down fixture
		beforeTearDown();
		log.info("Stopping Jetty");
		jettyServer.stop();
		log.info("Jetty stopped");
		// call for further tear down
		afterTearDown();
	}
}