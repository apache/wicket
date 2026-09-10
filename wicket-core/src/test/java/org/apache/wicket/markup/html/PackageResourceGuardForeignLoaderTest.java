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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link PackageResourceGuard} tells a component's markup template from a static html file that may
 * be served by looking for a class file of the same name beside it. Wicket is not always loaded by
 * a class loader that can see the application's classes: it can sit in a container's shared library
 * folder, in the library folder of an ear with the application in the war, in an application
 * server's global module, or in an osgi bundle that cannot resolve a class an application bundle
 * holds. The markup itself is found in those deployments all the same, because
 * {@link org.apache.wicket.core.util.resource.ClassPathResourceFinder ClassPathResourceFinder}
 * searches the class loader of the scope class and the current thread's context class loader as
 * well, so the guard has to look where the markup is looked for.
 * <p>
 * Every test here runs with the application's classes in a class loader of their own, a child of
 * the loader that holds wicket-core, with the context class loader pointing at it, and asserts that
 * the guard finds the class there.
 */
public class PackageResourceGuardForeignLoaderTest extends WicketTestCase
{
	/** The package the fixtures are written into, as a resource path. */
	private static final String PACKAGE = "org/apache/wicket/markup/html/";

	/** Only in the foreign class loader's {@code HiddenPanel.html}. */
	private static final String MARKUP = "markup-only-the-foreign-class-loader-can-see";

	/** Only in the foreign class loader's {@code HiddenPanel_nl.html}. */
	private static final String LOCALIZED_MARKUP = "dutch-markup-only-the-foreign-class-loader-can-see";

	/** Only in the foreign class loader's {@code guarded_snippet.html}. */
	private static final String SNIPPET = "static-html-only-the-foreign-class-loader-can-see";

	/** */
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private Path foreignClasspath;

	private URLClassLoader foreignLoader;

	private ClassLoader originalContextClassLoader;

	/**
	 * Writes the fixtures to a classpath of their own and hands it to a class loader below the one
	 * that holds wicket-core.
	 * <p>
	 * The fixtures cannot live in {@code src/test/java}: the build copies everything there that is
	 * not a java file onto the test classpath, where the scope class' own class loader would find
	 * them and the context class loader this test is about would never be reached.
	 *
	 * @throws IOException
	 */
	@Before
	public void deployTheApplicationBelowWicket() throws IOException
	{
		foreignClasspath = temporaryFolder.getRoot().toPath();
		Path pkg = Files.createDirectories(foreignClasspath.resolve(PACKAGE));

		// Nothing ever loads this class. The guard probes for the resource and never reads it, and
		// no part of serving a package resource resolves the class beside the markup.
		Files.write(pkg.resolve("HiddenPanel.class"),
			new byte[] { (byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE });
		write(pkg.resolve("HiddenPanel.html"), panel(MARKUP));
		write(pkg.resolve("HiddenPanel_nl.html"), panel(LOCALIZED_MARKUP));

		// and a static html file that is no component's markup: no class exists under its name or
		// under the shorter name the guard's underscore loop walks back to
		write(pkg.resolve("guarded_snippet.html"), "<span>" + SNIPPET + "</span>\n");

		// A child of the loader that holds wicket-core, which is what all of the affected
		// deployments amount to. A sibling loader would not do: a probe that misses because the
		// package is absent from it altogether says nothing about a parent that cannot see the
		// classes of its child.
		foreignLoader = new URLClassLoader(new URL[] { foreignClasspath.toUri().toURL() },
			PackageResourceGuard.class.getClassLoader());

		originalContextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(foreignLoader);
	}

	/**
	 * @throws IOException
	 */
	@After
	public void undeploy() throws IOException
	{
		Thread.currentThread().setContextClassLoader(originalContextClassLoader);
		foreignLoader.close();
	}

	private static void write(Path file, String content) throws IOException
	{
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
	}

	private static String panel(String marker)
	{
		return "<wicket:panel>\n\t<span>" + marker + "</span>\n</wicket:panel>\n";
	}

	private static String resourceUrl(String name)
	{
		return "wicket/resource/" + PackageResourceGuardForeignLoaderTest.class.getName() + '/' +
			name;
	}

	/**
	 * The premise of every test below, asserted rather than assumed. Each of them refuses a path
	 * that the check which is already there would refuse anyway if wicket's own class loader could
	 * see the fixture's class, and accepts one that has no class under any prefix of its name in
	 * any loader.
	 */
	@Test
	public void theForeignClasspathIsShapedAsTheTestsAssume()
	{
		ClassLoader wicketsOwnLoader = PackageResourceGuard.class.getClassLoader();

		assertSame(
			"the application's class loader has to be a child of the loader that holds wicket-core",
			wicketsOwnLoader, foreignLoader.getParent());

		assertNull(
			"wicket's own class loader must not see the fixture's class, or the guard would refuse "
				+ "the markup on the check that is already there and these tests would pass "
				+ "without covering anything",
			wicketsOwnLoader.getResource(PACKAGE + "HiddenPanel.class"));
		assertNull(wicketsOwnLoader.getResource(PACKAGE + "HiddenPanel.html"));

		assertNotNull(foreignLoader.getResource(PACKAGE + "HiddenPanel.class"));
		assertNotNull(foreignLoader.getResource(PACKAGE + "HiddenPanel.html"));
		assertNotNull(foreignLoader.getResource(PACKAGE + "HiddenPanel_nl.html"));

		assertNotNull(foreignLoader.getResource(PACKAGE + "guarded_snippet.html"));
		assertNull("the static html must have no class of its own, or it would not be static html",
			foreignLoader.getResource(PACKAGE + "guarded_snippet.class"));
		assertNull("nor a class under the name the underscore loop walks back to",
			foreignLoader.getResource(PACKAGE + "guarded.class"));
	}

	/**
	 * The class file is in the application's class loader, not in wicket's, so a guard that asks
	 * only its own class loader finds none and serves a component's markup template.
	 */
	@Test
	public void markupOfAComponentOnlyTheApplicationsClassLoaderSeesIsDenied()
	{
		assertFalse(new PackageResourceGuard().accept(PACKAGE + "HiddenPanel.html"));
	}

	/**
	 * The same for a name the guard has to strip a locale off before it reaches the name the class
	 * carries.
	 */
	@Test
	public void markupTheUnderscoreLoopHasToWalkBackToIsDenied()
	{
		assertFalse(new PackageResourceGuard().accept(PACKAGE + "HiddenPanel_nl.html"));
	}

	/**
	 * Widening the search must not turn static html into a component's markup: an underscore in the
	 * name is not by itself a reason to refuse a file for which no class exists anywhere.
	 */
	@Test
	public void staticMarkupWithNoClassUnderAnyPrefixIsStillAccepted()
	{
		assertTrue(new PackageResourceGuard().accept(PACKAGE + "guarded_snippet.html"));
	}

	/**
	 * That the refusal is really the application's class loader being consulted, and not something
	 * else about the fixture: with the context class loader back where it was, there is no class to
	 * find and the very same path is accepted.
	 */
	@Test
	public void theDenialComesFromTheApplicationsClassLoader()
	{
		assertFalse(new PackageResourceGuard().accept(PACKAGE + "HiddenPanel.html"));

		Thread.currentThread().setContextClassLoader(originalContextClassLoader);

		assertTrue(
			"with the application's class loader out of reach there is no class to find, so this "
				+ "path is accepted: the refusal above can only have come from that loader",
			new PackageResourceGuard().accept(PACKAGE + "HiddenPanel.html"));
	}

	/**
	 * The whole request cycle, the way a client asks for it. Wicket writes the fully qualified name
	 * of the scope class into the url of every package resource it renders, so the name is known to
	 * every client.
	 */
	@Test
	public void componentMarkupIsNotServedThroughAPackageResourceUrl()
	{
		try
		{
			tester.executeUrl(resourceUrl("HiddenPanel.html"));
			fail("the markup template of a component must not be served as a package resource");
		}
		catch (PackageResource.PackageResourceBlockedException expected)
		{
		}
	}

	/**
	 * The control for the test above: a static html file in the same class loader is served over
	 * the same kind of url. Without this, a request that fails for any other reason would look like
	 * the guard doing its job.
	 */
	@Test
	public void staticMarkupOnlyTheApplicationsClassLoaderSeesIsStillServed()
	{
		tester.executeUrl(resourceUrl("guarded_snippet.html"));

		String response = tester.getLastResponseAsString();
		assertTrue(response, response.contains(SNIPPET));
	}
}
