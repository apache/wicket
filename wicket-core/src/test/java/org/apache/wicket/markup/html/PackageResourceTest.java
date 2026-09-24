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
package org.apache.wicket.markup.html;

import org.apache.wicket.Application;
import org.apache.wicket.SharedResources;
import org.apache.wicket.markup.html.snake_case.TestPageInsideSnakeCasePackage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.https.HttpPage;
import org.apache.wicket.request.resource.JavaScriptPackageResource;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;


/**
 * Tests for package resources.
 * 
 * @author Eelco Hillenius
 */
public class PackageResourceTest extends WicketTestCase
{
	/** The package the ReportPanel fixtures live in. */
	private static final String PACKAGE = "/org/apache/wicket/markup/html/";

	/** Only in {@code ReportPanel_v2.html}. */
	private static final String STYLED_MARKUP = "markup-of-report-panel-v2";

	/** Only in {@code ReportPanel_v2_ja.html}. */
	private static final String LOCALIZED_MARKUP = "localized-markup-of-report-panel-v2";

	/** mock application object */
	public WebApplication application;

	/**
	 *
	 */
	@Before
	public void before()
	{
		application = tester.getApplication();
	}

	/**
	 * Tests binding a single absolute package resource.
	 * 
	 * @throws Exception
	 */
	@Test
	public void bindAbsolutePackageResource() throws Exception
	{
		final SharedResources sharedResources = Application.get().getSharedResources();
		assertNotNull("resource packaged1.txt should be available as a packaged resource",
			sharedResources.get(PackageResourceTest.class, "packaged1.txt", null, null, null, true));
	}

	/**
	 * Tests {@link PackageResourceGuard}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void packageResourceGuard() throws Exception
	{
		PackageResourceGuard guard = new PackageResourceGuard();
		assertTrue(guard.acceptExtension("txt"));
		assertFalse(guard.acceptExtension("java"));
		assertTrue(guard.accept("foo/Bar.txt"));
		assertFalse(guard.accept("foo/Bar.java"));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, "Bar.txt")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, "Bar.txt.")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, ".Bar.txt")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, ".Bar.txt.")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, ".Bar")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, ".java")));
		assertFalse(guard.accept(Packages.absolutePath(PackageResourceTest.class, "Bar.java")));
		assertTrue(guard.accept(Packages.absolutePath(PackageResourceTest.class, "foo/.java")));
	}

	/**
	 * Test lenient matching
	 * 
	 * @throws Exception
	 */
	@Test
	public void lenientPackageResourceMatching() throws Exception
	{
		ResourceReference invalidResource = new PackageResourceReference(PackageResourceTest.class,
			"i_do_not_exist.txt", Locale.ENGLISH, null, null);
		assertNotNull(
			"resource i_do_not_exist.txt SHOULD be available as a packaged resource even if it doesn't exist",
			invalidResource.getResource());

		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1.txt", null, null,
			null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1.txt", Locale.CHINA,
			null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1.txt", Locale.CHINA,
			"foo", null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1.txt", null, "foo",
			null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en.txt", null,
			null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en_US.txt", null,
			null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en_US.txt", null,
			"foo", null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en_US.txt",
			Locale.US, null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en_US.txt",
			Locale.CANADA, null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_en_US.txt",
			Locale.CHINA, null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_foo_bar_en.txt",
			null, null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class, "packaged1_foo_bar_en_US.txt",
			null, null, null));
		assertTrue(PackageResource.exists(PackageResourceTest.class,
			"packaged1_foo_bar_en_US_MAC.txt", null, null, null));

		tester.getRequest().setUrl(tester.getRequestCycle().mapUrlFor(invalidResource, null));
		// since the resource does not exist wicket should let the handling fall through to the next
		// filter/servlet which will cause a 404 later
		assertFalse(tester.processRequest());

	}

	/**
	 * Test that {@link PackageResource} writes its Content-Type header in the response
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-4119
	 */
	@Test
	public void contentType()
	{
		PackageResource textResource = new PackageResource(PackageResourceTest.class,
			"packaged1.txt", null, null, null)
		{
			private static final long serialVersionUID = 1L;
		};

		tester.startResource(textResource);
		assertEquals("text/plain", tester.getLastResponse().getContentType());

		PackageResource jsResource = new PackageResource(PackageResourceTest.class, "packaged3.js",
			null, null, null)
		{
			private static final long serialVersionUID = 1L;
		};

		tester.startResource(jsResource);
		assertEquals("text/javascript", tester.getLastResponse().getContentType());
	}

	@Test
	public void textFileWithEncoding()
	{
		final String encoding = "Klingon-8859-42";
		final PackageResource resource = new PackageResource(PackageResourceTest.class,
			"packaged1.txt", null, null, null)
		{
			private static final long serialVersionUID = 1L;
		};
		resource.setTextEncoding(encoding);
		tester.startResource(resource);
		final String contentType = tester.getLastResponse().getContentType();
		assertEquals("text/plain; charset=" + encoding, contentType);
	}

	@Test
	public void javascriptFileWithEncoding()
	{
		final String encoding = "Klingon-8859-42";
		final JavaScriptPackageResource resource = new JavaScriptPackageResource(
			PackageResourceTest.class, "packaged3.js", null, null, null)
		{
			private static final long serialVersionUID = 1L;
		};
		resource.setTextEncoding(encoding);
		tester.startResource(resource);
		final String contentType = tester.getLastResponse().getContentType();
		assertEquals("text/javascript; charset=" + encoding, contentType);
	}

	@Test
	public void getResourceStream()
	{
		PackageResource resource = new PackageResourceReference(PackageResourceTest.class,
			"packaged1.txt").getResource();
		assertNotNull(resource.getResourceStream());
	}

	@Test(expected = PackageResource.PackageResourceBlockedException.class)
	public void dontGetResourceStream()
	{
		PackageResource resource = new PackageResourceReference(HttpPage.class,
			"HttpPage.html").getResource();
		resource.getResourceStream();
	}

	@Test(expected = PackageResource.PackageResourceBlockedException.class)
	public void dontGetResourceStreamIfNameHasSuffix()
	{
		PackageResource resource = new PackageResourceReference(HttpPage.class,
			"HttpPage_en.html").getResource();
		resource.getResourceStream();
	}

	@Test
	public void getResourceStreamInSnakeCasePackage()
	{
		PackageResource resource = new PackageResourceReference(
			TestPageInsideSnakeCasePackage.class, "style.css").getResource();
		assertNotNull(resource.getResourceStream());
	}

	@Test(expected = PackageResource.PackageResourceBlockedException.class)
	public void dontGetResourceStreamInSnakeCasePackage()
	{
		PackageResource resource = new PackageResourceReference(
			TestPageInsideSnakeCasePackage.class,
			"TestPageInsideSnakeCasePackage.html").getResource();
		resource.getResourceStream();
	}

	@Test(expected = PackageResource.PackageResourceBlockedException.class)
	public void dontGetResourceStreamInSnakeCasePackageIfNameHasSuffix()
	{
		PackageResource resource = new PackageResourceReference(
			TestPageInsideSnakeCasePackage.class,
			"TestPageInsideSnakeCasePackage_en.html").getResource();
		resource.getResourceStream();
	}

	/**
	 * The premise of every ReportPanel test below, asserted rather than assumed.
	 * <p>
	 * They all ask for {@code ReportPanel.html}, a name the guard accepts because nothing under
	 * that base name is a class, and rely on the style, the variation or the locale completing it
	 * into a name that is. Were a {@code ReportPanel} class ever added to this package, the guard
	 * would refuse the requested name outright, every one of those tests would keep passing on
	 * that older check alone, and the completed name they exist to cover would go untested with
	 * nothing to show it.
	 */
	@Test
	public void reportPanelFixturesAreShapedAsTheTestsBelowAssume() throws IOException
	{
		assertNull("a ReportPanel class makes the guard refuse ReportPanel.html on the requested name, "
			+ "so the tests below would no longer cover the name the attributes complete",
			getClass().getResource(PACKAGE + "ReportPanel.class"));
		assertNull("ReportPanel.html must not exist, or the locator would resolve it instead of the "
			+ "name the attributes complete",
			getClass().getResource(PACKAGE + "ReportPanel.html"));

		// the classes whose markup the attributes reach, and which the guard has to find
		assertNotNull(getClass().getResource(PACKAGE + "ReportPanel_v2.class"));
		assertNotNull(getClass().getResource(PACKAGE + "ReportPanel_nl.class"));

		// their markup, carrying the markers the request-cycle tests look for in the response
		assertTrue(markupOf("ReportPanel_v2.html").contains(STYLED_MARKUP));
		assertTrue(markupOf("ReportPanel_v2_ja.html").contains(LOCALIZED_MARKUP));

		// and the static html that has to stay servable, with no class under any prefix of it
		assertNotNull(getClass().getResource(PACKAGE + "nonComponent_snippet.html"));
		assertNull(getClass().getResource(PACKAGE + "nonComponent_snippet.class"));
		assertNull(getClass().getResource(PACKAGE + "nonComponent.class"));
	}

	private String markupOf(String name) throws IOException
	{
		try (InputStream markup = getClass().getResourceAsStream(PACKAGE + name))
		{
			assertNotNull(name + " is missing from the test classpath", markup);

			return IOUtils.toString(markup, "UTF-8");
		}
	}

	/**
	 * The style, the variation and the locale are appended to the name before the extension, so a
	 * name that is not a markup file itself can be completed into one. There is no
	 * {@code ReportPanel} class, so the name that is asked for is accepted, while
	 * {@link ReportPanel_v2} and {@link ReportPanel_nl} are components whose markup must not be
	 * served.
	 */
	@Test(expected = PackageResource.PackageResourceBlockedException.class)
	public void dontGetResourceStreamWhenStyleCompletesTheName()
	{
		PackageResource resource = new PackageResourceReference(PackageResourceTest.class,
			"ReportPanel.html", null, "v2", null).getResource();
		resource.getResourceStream();
	}

	@Test(expected = PackageResource.PackageResourceBlockedException.class)
	public void dontGetResourceStreamWhenVariationCompletesTheName()
	{
		PackageResource resource = new PackageResourceReference(PackageResourceTest.class,
			"ReportPanel.html", null, null, "v2").getResource();
		resource.getResourceStream();
	}

	@Test(expected = PackageResource.PackageResourceBlockedException.class)
	public void dontGetResourceStreamWhenLocaleCompletesTheName()
	{
		PackageResource resource = new PackageResourceReference(PackageResourceTest.class,
			"ReportPanel.html", new Locale("nl"), null, null).getResource();
		resource.getResourceStream();
	}

	/**
	 * A style and a locale together, reaching the localized markup of the same component:
	 * {@code ReportPanel.html} plus the style {@code v2} and the locale {@code ja} completes into
	 * {@code ReportPanel_v2_ja.html}, which is {@link ReportPanel_v2}'s Japanese template. The
	 * guard's underscore loop has to walk back two suffixes to find the class.
	 */
	@Test(expected = PackageResource.PackageResourceBlockedException.class)
	public void dontGetResourceStreamWhenStyleAndLocaleCompleteTheName()
	{
		PackageResource resource = new PackageResourceReference(PackageResourceTest.class,
			"ReportPanel.html", new Locale("ja"), "v2", null).getResource();
		resource.getResourceStream();
	}

	@Test
	public void componentMarkupIsNotServedThroughAStyledAndLocalizedUrl()
	{
		tester.executeUrl("wicket/resource/" + PackageResourceTest.class.getName() +
			"/ReportPanel.html?ja-v2");

		assertFalse(tester.getLastResponseAsString().contains(LOCALIZED_MARKUP));
	}

	/**
	 * A resource that a style completes into a component's markup does not exist as a package
	 * resource, so no resource reference is created for it either.
	 */
	@Test
	public void dontExistWhenStyleCompletesTheName()
	{
		assertFalse(PackageResource.exists(PackageResourceTest.class, "ReportPanel.html", null,
			"v2", null));
	}

	/**
	 * The whole request cycle, with the style in the name of the first query parameter, the way a
	 * client sends it.
	 */
	@Test
	public void componentMarkupIsNotServedThroughAStyledUrl()
	{
		tester.executeUrl("wicket/resource/" + PackageResourceTest.class.getName() +
			"/ReportPanel.html?-v2");

		assertFalse(tester.getLastResponseAsString().contains(STYLED_MARKUP));
	}

	/**
	 * An underscore in the name of an html package resource is not by itself a reason to refuse
	 * it: only a name for which a class exists is a markup template.
	 */
	@Test
	public void getResourceStreamForStaticMarkupWithAnUnderscore()
	{
		PackageResource resource = new PackageResourceReference(PackageResourceTest.class,
			"nonComponent_snippet.html").getResource();
		assertNotNull(resource.getResourceStream());
	}

}
