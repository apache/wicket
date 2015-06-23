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
package org.apache.wicket.arquillian.testing.servletcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.wicket.arquillian.testing.TestWicketJavaEEApplication;
import org.apache.wicket.arquillian.testing.deployment.AbstractDeploymentTest;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>WARNING: If this error occurs - org.jboss.arquillian.container.spi.client.container.LifecycleException: The server is already running! Managed containers do not support connecting to running server instances due to the possible harmful effect of connecting to the wrong server. Please stop server before running or change to another type of container.
 *	To disable this check and allow Arquillian to connect to a running server, set allowConnectingToRunningServer to true in the container configuration.</b>
 *	
 *	<b>SOLUTION: Search and kill wildfly or jboss proccess instance that are using port 8080.</b>
 * 
 * <b> If you can't run inside eclipse, add as source the folder src/test/resources and try again. </b>
 * 
 * @author felipecalmeida
 * @since 06/23/2015
 *
 */
@RunWith(Arquillian.class)
public class ArquillianContainerProvidedTest extends AbstractDeploymentTest {
	
	private static final Logger log = LoggerFactory.getLogger(ArquillianContainerProvidedTest.class);

	/**
	 * Using container's servlet context and/or filter provided configured in web.xml and using Arquillian.
	 */
	@Test
	public void testFindResourcesServletContextFromContainer() {
		WebApplication webApplication =  useServletContextContainer();
		setWicketTester(new WicketTester(webApplication, webApplication.getServletContext(), false));
		findResourcesServletContext();
	}
	
	/**
	 * Using container's servlet context and/or filter provided configured in web.xml and using Arquillian.
	 */
	@Test
	public void testFindResourcesWebApplicationFromContainer() {
		WebApplication webApplication = useServletContextContainer();
		setWicketTester(new WicketTester(webApplication, false));
		findResourcesServletContext();
	}
	
	/**
	 * Creating another application and trying to reuse the ServletContext/Filter.
	 */
	@Test
	public void testNewApplicationTryReuseServletContextFilter() {
		try {
			log.info("Trying to reuse container's ServletContext/Filter.");
			setWicketTester(new WicketTester(new TestWicketJavaEEApplication(),false));
		} catch (IllegalStateException e) {
			assertEquals("servletContext is not set yet. Any code in your Application object that uses the wicket filter instance should be put in the init() method instead of your constructor",e.getMessage());
			log.error("Cannot use container's ServletContext.\n", e);
		}
		
		assertNull(wicketTester);
	}
	
	/**
	 * Null application to test error.
	 */
	@Test
	public void testNullApplication() {
		try {
			log.info("Trying to use a null application.");
			setWicketTester(new WicketTester(null,false));
		} catch (AssertionError e) {
			assertEquals("WebApplication cannot be null",e.getMessage());
			log.error("WebApplication cannot be null\n", e);
			assertNull(wicketTester);
		}
		
	}
	
	/**
	 * Test with new application.
	 */
	@Test
	public void testNewApplication() {
		try {
			setWicketTester(new WicketTester(new TestWicketJavaEEApplication()));
			assertNotNull(getWicketTester().getApplication());
			log.info("Using mock servletcontext.");
			log.info("WebApplication MOCK after wicketTester Name: " + getWicketTester().getApplication().getName());
			log.info("ServletContext MOCK after wicketTester Name: " + getWicketTester().getServletContext().getServletContextName());
			log.info("Server info: " + getWicketTester().getServletContext().getServerInfo());
			assertEquals("Wicket Mock Test Environment v1.0", getWicketTester().getServletContext().getServerInfo());
		} catch (IllegalStateException e) {
			// I don't know what or if could cause this.
			fail("Cannot use a mock ServletContext.");
		}
		
		// USING MOCK.
		findResourcesServletContext();
	}

	/**
	 * Look for resources (like html, js, css, img, etc).
	 * 
	 * @param servletContext
	 */
	private void findResourcesServletContext() {
		try {
			// Doing the same thing that ResourceWebApplicationPath does.
			URL resource = getWicketTester().getServletContext().getResource("/pages/InsertContact.html");
			if(resource == null) {
				throw new MalformedURLException("Resource /pages/InsertContact.html not found.");
			}
			log.info("Resource found " + resource.getFile());
			assertTrue(resource.getFile().contains("/pages/InsertContact.html"));
		} catch (Exception e) {
			assertEquals("Resource /pages/InsertContact.html not found.", e.getMessage());
			log.error("Resource cannot be found.", e);
		}
	}

}
