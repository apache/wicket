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
package org.apache.wicket.arquillian.testing.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.wicket.arquillian.testing.TestWicketJavaEEApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
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
public abstract class AbstractDeploymentTest {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractDeploymentTest.class);
	
	private static final String WEBAPP_SRC = "src/main/webapp";
	private static final String WEBAPP_TEST_SRC = "src/test/webapp";

	@Deployment
	public static WebArchive deployment() {
		// Create webapp files from src/main/webapp. (WEB AS NORMAL)
		GenericArchive webapp = ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class).importDirectory(WEBAPP_SRC).as(GenericArchive.class);
		
		// Create webapptest files from src/test/webapp. (TEST)
		GenericArchive webappTest = ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class).importDirectory(WEBAPP_TEST_SRC).as(GenericArchive.class);
		
		// Some configurations to create a manifest.mf.
		ByteArrayAsset resource = new ByteArrayAsset("Dependencies: org.jboss.msc".getBytes());
		
		// Create libs from POM.XML.
		File[] asFile = Maven.configureResolver()
				// I'm not using internet directly, I have a Nexus Repository that handles the libs for me.
				.workOffline().withMavenCentralRepo(false)
				// Load everything from pom, that is compile and runtime, as a file and transitivity to don't loose anything and cause exceptions.
				.loadPomFromFile("./pom.xml").importCompileAndRuntimeDependencies()
				.resolve().withTransitivity().asFile();
		
		// Create the WAR.
		return ShrinkWrap.create(WebArchive.class, "wicket-servletContext.war")
						 // Add packages and/or classes.
						 .addPackages(true, TestWicketJavaEEApplication.class.getPackage())
						 // Add the persistence.xml
						 .addAsResource("META-INF/persistence.xml")
						 // Add a manifest.
						 .addAsManifestResource(resource, "MANIFEST.MF")
						 // Add WEBAPP files.
						 .merge(webapp,"/",Filters.exclude(".*\\web.xml"))
						 // Add WEBAPP TEST files.
						 .merge(webappTest,"/",Filters.includeAll())
						 // Add LIBS from POM.XML.
						 .addAsLibraries(asFile);
	}
	
	protected WicketTester wicketTester;
	
	/**
	 * Set a new instance of wicketTester.
	 *
	 * @param wicketTester
	 */
	public void setWicketTester(WicketTester wicketTester) {
		this.wicketTester = wicketTester;
	}
	
	/**
	 * Get an instance of wicketTester. The first time verify and create a new one.
	 *
	 * @return
	 */
	public WicketTester getWicketTester() {
		if(wicketTester == null) {
			wicketTester = new WicketTester(useServletContextContainer(),false);
		}
		return wicketTester;
	}
	
	/**
	 * Loading the TestWicketJavaEEApplication from src/test/webapp/WEB-INF/web.xml.
	 * 
	 * @return
	 */
	protected WebApplication useServletContextContainer() {
		WebApplication webApplication = null;
		try {
			webApplication = TestWicketJavaEEApplication.get();
		} catch (Exception e) {
			log.error("If not using arquillian, maybe org.apache.wicket.Application has a message for you " + e.getMessage());
			// I didn't test it, just supposing that i'm not using arquillian, so should show this message.
			assertEquals("There is no application attached to current thread " + Thread.currentThread().getName(), e.getMessage());
		}
		
		assertNotNull(webApplication);
		
		log.info("WebApplication Name: " + webApplication.getName());
		
		ServletContext servletContext = webApplication.getServletContext();
		assertNotNull(servletContext);
		log.info("ServletContext Name: " + servletContext.getServletContextName());
		assertEquals("Wicket Arquillian WildFly Quickstart: Wicket TEST WAR",servletContext.getServletContextName());
		
		log.info("Server info: " + servletContext.getServerInfo());
		return webApplication;
	}

}
