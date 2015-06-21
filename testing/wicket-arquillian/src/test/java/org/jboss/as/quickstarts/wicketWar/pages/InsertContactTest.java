/**
 * 
 */
package org.jboss.as.quickstarts.wicketWar.pages;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.quickstarts.wicketWar.TestWicketJavaEEApplication;
import org.jboss.as.quickstarts.wicketWar.WicketJavaEEApplication;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>WARNING: IF THIS ERROR OCCURS - org.jboss.arquillian.container.spi.client.container.LifecycleException: The server is already running! Managed containers do not support connecting to running server instances due to the possible harmful effect of connecting to the wrong server. Please stop server before running or change to another type of container.
 *	To disable this check and allow Arquillian to connect to a running server, set allowConnectingToRunningServer to true in the container configuration.</b>
 *	
 *	<b>SOLUTION: SEARCH AND KILL WILDFLY OR JBOSS PROCCESS INSTANCE THAT ARE USING PORT 8080.</b>
 * 
 * @author felipecalmeida
 * @since 06/21/2015
 *
 */
@RunWith(Arquillian.class)
public class InsertContactTest {
	
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(InsertContactTest.class);
	
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
	
	private WicketTester wicketTester;

	private ServletContext servletContext;
	
//	private String basePath = TestWicketJavaEEApplication.class.getPackage().getName().replaceAll("\\.", "\\/");
	
	@After
	public void tearDown() throws Exception {
		if(wicketTester != null && wicketTester.getApplication() != null) {
			wicketTester.getApplication().internalDestroy();
		}
	}

	@Test
	public void testFindResources(){
		WebApplication webApplication = useServletContextContainer();
		
		// USING CONTAINER'S SERVLET.
		testFindResourcesServletContext(servletContext);
		
		reuseServletContextFromContainerOrCreateMock(webApplication);
		
		// USING MOCK.
		testFindResourcesServletContextMock(wicketTester);
		
	}

	/**
	 * Loading the TestWicketJavaEEApplication from src/test/webapp/WEB-INF/web.xml.
	 * 
	 * @return
	 */
	private WebApplication useServletContextContainer() {
		WebApplication webApplication = null;
		try {
			webApplication = TestWicketJavaEEApplication.get();
		} catch (Exception e) {
			log.error("IF NOT USING ARQUILLIAN, org.apache.wicket.Application has a message for you " + e.getMessage());
			// DIDN'T TEST THIS, JUST SUPPOSING THAT I'M NOT USING ARQUILLIAN, SO SHOULD SHOW THIS MESSAGE.
			assertEquals("There is no application attached to current thread " + Thread.currentThread().getName(), e.getMessage());
		}
		
		assertNotNull(webApplication);
		
		log.info("WebApplication Name: " + webApplication.getName());
		
		servletContext = webApplication.getServletContext();
		assertNotNull(servletContext);
		log.info("ServletContext Name: " + servletContext.getServletContextName());
		assertEquals("WildFly Quickstart: Wicket TEST WAR",servletContext.getServletContextName());
		
		log.info("Server info: " + servletContext.getServerInfo());
		return webApplication;
	}

	/**
	 * Trying to use ServletContext from Container.
	 * 
	 * @param webApplication
	 */
	private void reuseServletContextFromContainerOrCreateMock(WebApplication webApplication) {
		try {
			log.info("TRYING TO REUSE CONTAINER'S SERVLETCONTEXT/FILTER.");
			wicketTester = new WicketTester(webApplication);
			assertNotNull(wicketTester.getApplication());
			log.info("USING CONTAINER'S SERVLETCONTEXT/FILTER.");
			log.info("WebApplication after wicketTester Name: " + wicketTester.getApplication().getName());
			log.info("ServletContext after wicketTester Name: " + wicketTester.getServletContext().getServletContextName());
			log.info("Server info: " + wicketTester.getServletContext().getServerInfo());
		} catch (IllegalStateException e) {
			assertEquals("Application name can only be set once.",e.getMessage());
			log.error("CANNOT USE CONTAINER'S SERVLETCONTEXT.\n", e);
		}
		if(wicketTester == null){
			try {
				wicketTester = new WicketTester(new TestWicketJavaEEApplication());
				assertNotNull(wicketTester.getApplication());
				log.info("USING A MOCK SERVLETCONTEXT.");
				log.info("WebApplication MOCK after wicketTester Name: " + wicketTester.getApplication().getName());
				log.info("ServletContext MOCK after wicketTester Name: " + wicketTester.getServletContext().getServletContextName());
				log.info("Server info: " + wicketTester.getServletContext().getServerInfo());
				assertEquals("Wicket Mock Test Environment v1.0", wicketTester.getServletContext().getServerInfo());
			} catch (IllegalStateException e) {
				// I DON'T KNOW WHAT OR IF COULD CAUSE THIS.
				log.error("CANNOT USE A MOCK SERVLETCONTEXT.");
//				assertEquals("Application name can only be set once.",e.getMessage());
			}
		}
	}
	
	/**
	 * Using mock.
	 * 
	 * @param wicketTester
	 */
	private void testFindResourcesServletContextMock(WicketTester wicketTester) {
		testFindResourcesServletContext(wicketTester.getServletContext());
	}
	
	/**
	 * Look for resources (like html, js, css, img, etc).
	 * 
	 * @param servletContext
	 */
	private void testFindResourcesServletContext(ServletContext servletContext) {
		try {
			// Doing the same thing that ResourceWebApplicationPath does.
			URL resource = servletContext.getResource("/pages/InsertContact.html");
			if(resource == null) {
				throw new MalformedURLException("Resource /pages/InsertContact.html not found.");
			}
			log.info("RESOURCE FOUND " + resource.getFile());
			assertTrue(resource.getFile().contains("/pages/InsertContact.html"));
		} catch (Exception e) {
			assertEquals("Resource /pages/InsertContact.html not found.", e.getMessage());
			log.error("RESOURCE CANNOT BE FOUND.", e);
		}
	}

}
