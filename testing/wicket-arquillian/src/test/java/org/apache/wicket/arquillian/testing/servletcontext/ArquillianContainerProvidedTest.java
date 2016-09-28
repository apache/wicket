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

	private static final String RESOURCE_PAGES_INSERT_CONTACT_HTML_NOT_FOUND = "Resource /pages/InsertContact.html not found.";

	/**
	 * Using container's servlet context and/or filter provided configured in web.xml and using Arquillian.
	 */
	@Test
	public void testFindResourcesServletContextFromContainer() throws MalformedURLException
	{
		WebApplication webApplication =  useServletContextContainer();
		setWicketTester(new WicketTester(webApplication, webApplication.getServletContext(), false));
		findResourcesServletContext();
	}
	
	/**
	 * Using container's servlet context and/or filter provided configured in web.xml and using Arquillian.
	 */
	@Test
	public void testFindResourcesWebApplicationFromContainer() throws MalformedURLException
	{
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
			setWicketTester(new WicketTester(new TestWicketJavaEEApplication(), false));
			fail("Should not be able to reuse the servlet context");
		} catch (IllegalStateException e) {
			assertEquals("servletContext is not set yet. Any code in your Application object that uses the wicket filter instance should be put in the init() method instead of your constructor", e.getMessage());
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
			setWicketTester(new WicketTester(null, false));
			fail("WebApplication cannot be null");
		} catch (IllegalArgumentException iax) {
			assertEquals("Argument 'application' may not be null.", iax.getMessage());
		}
		assertNull(wicketTester);
	}
	
	/**
	 * Test with new application.
	 */
	@Test
	public void testNewApplication() throws MalformedURLException
	{
		setWicketTester(new WicketTester(new TestWicketJavaEEApplication()));
		assertNotNull(getWicketTester().getApplication());
		log.info("Using mock servletcontext.");
		log.info("WebApplication MOCK after wicketTester Name: " + getWicketTester().getApplication().getName());
		log.info("ServletContext MOCK after wicketTester Name: " + getWicketTester().getServletContext().getServletContextName());
		log.info("Server info: " + getWicketTester().getServletContext().getServerInfo());
		assertEquals("Wicket Mock Test Environment v1.0", getWicketTester().getServletContext().getServerInfo());

		// USING MOCK.
		try
		{
			findResourcesServletContext();
			fail("Should not be able to find '/pages/InsertContact.html' in the mocked servlet context");
		}
		catch (IllegalStateException isx)
		{
			assertEquals(RESOURCE_PAGES_INSERT_CONTACT_HTML_NOT_FOUND, isx.getMessage());
		}
	}

	/**
	 * Look for resources (like html, js, css, img, etc).
	 */
	private void findResourcesServletContext() throws MalformedURLException
	{
		// Doing the same thing that ResourceWebApplicationPath does.
		URL resource = getWicketTester().getServletContext().getResource("/pages/InsertContact.html");
		if (resource == null) {
			throw new IllegalStateException(RESOURCE_PAGES_INSERT_CONTACT_HTML_NOT_FOUND);
		}
		log.info("Resource found " + resource.getFile());
		assertTrue(resource.getFile().contains("/pages/InsertContact.html"));
	}

}
