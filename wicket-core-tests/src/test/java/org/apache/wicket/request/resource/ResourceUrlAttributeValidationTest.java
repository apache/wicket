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
package org.apache.wicket.request.resource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the locale, style and variation decoded from a resource URL cannot change the
 * directory a resource is resolved in.
 * <p>
 * The resource stream locator appends these three attributes to the path it asks every
 * {@link IResourceFinder} for, as {@code <path>_<variation>_<style>_<locale>.<extension>}. A value
 * containing a path separator would therefore contribute more than one path component, and the
 * lookup would resolve somewhere other than the package the resource belongs to. No legitimate
 * locale, style or variation contains one, so {@code ResourceUtil#decodeResourceReferenceAttributes}
 * drops such a value.
 * <p>
 * These tests replace the application's resource finders with a recording one, so that the paths
 * Wicket asks for can be asserted directly, independently of how a servlet container resolves them.
 * {@link NormalizingFinder} additionally collapses {@code ..} the way a container does when
 * resolving a path. {@code MockServletContext} does not: it resolves through
 * {@code new File(root, name)}, which will not walk through a {@code PublicPage_..} component that
 * does not exist, so a test relying on the mock context would not exercise this at all.
 */
public class ResourceUrlAttributeValidationTest extends WicketTestCase
{
	/** Served by {@link NormalizingFinder} for a path that resolved outside the package. */
	private static final String OUTSIDE_CONTENT = "content-from-another-location";

	/** Where the style below points, relative to the root the finder resolves against. */
	private static final String OUTSIDE_PREFIX = "other-package/";

	private static final String OUTSIDE_NAME = OUTSIDE_PREFIX + "Config";

	private static final String PACKAGE_PREFIX =
		ResourceUrlAttributeValidationTest.class.getPackageName().replace('.', '/') + "/";

	/**
	 * For every sub-package we need to move up one directory with {@code ../}, plus one for the
	 * style and one for the component.
	 */
	private static final int PARENT_STEPS =
		ResourceUrlAttributeValidationTest.class.getPackageName().split("\\.").length + 2;

	/**
	 * The style is carried in the first query parameter's NAME, with an empty value. The {@code -} is
	 * the attribute separator, so a {@code -} within the style itself has to be written as
	 * {@code ~}: {@code ResourceUtil#unescapeAttributesSeparator} turns {@code (\w)~(\w)} back into
	 * {@code -} after the split. Keeping one here means the validation is also shown to happen after
	 * that restoration rather than before it.
	 */
	private static final String STYLE_WITH_SEPARATORS =
		"en-" + "..%2F".repeat(PARENT_STEPS) + OUTSIDE_NAME.replace("-", "~").replace("/", "%2F");

	private static final String URL = "wicket/resource/" +
		ResourceUrlAttributeValidationTest.class.getName() + "/PublicPage.html?" +
		STYLE_WITH_SEPARATORS;

	/** Referenced by the resource URL above; it does not need to exist as a file. */
	public static class PublicPage extends WebPage
	{
	}

	/** Records every path the locator asks for. */
	private static class RecordingFinder implements IResourceFinder
	{
		final List<String> asked = new ArrayList<>();

		@Override
		public IResourceStream find(Class<?> clazz, String pathname)
		{
			asked.add(pathname);
			return found(pathname);
		}

		IResourceStream found(String pathname)
		{
			return null;
		}
	}

	/**
	 * Collapses {@code ..} in the path string the way a servlet container does when resolving it, and
	 * returns content for a path that ends up under {@link #OUTSIDE_PREFIX} - that is, for a lookup
	 * that left the package it started in.
	 */
	private static class NormalizingFinder extends RecordingFinder
	{
		@Override
		IResourceStream found(String pathname)
		{
			return normalize(pathname).startsWith(OUTSIDE_PREFIX)
				? new StringResourceStream(OUTSIDE_CONTENT)
				: null;
		}

		private static String normalize(String path)
		{
			List<String> segments = new ArrayList<>();
			for (String segment : path.split("/", -1))
			{
				if (segment.isEmpty() || ".".equals(segment))
				{
					continue;
				}
				if ("..".equals(segment))
				{
					if (segments.isEmpty() == false)
					{
						segments.remove(segments.size() - 1);
					}
					continue;
				}
				segments.add(segment);
			}
			return String.join("/", segments);
		}
	}

	private <T extends RecordingFinder> T install(T finder)
	{
		List<IResourceFinder> finders = tester.getApplication()
			.getResourceSettings()
			.getResourceFinders();
		finders.clear();
		finders.add(finder);
		return finder;
	}

	/**
	 * A rejected style is dropped rather than failing the request, so the resource is still located
	 * under its unstyled name.
	 */
	@Test
	void unstyledResourceIsStillLocatedWhenStyleIsRejected()
	{
		RecordingFinder finder = install(new RecordingFinder());

		tester.executeUrl(URL);

		assertTrue(finder.asked.contains(PACKAGE_PREFIX + "PublicPage.html"),
			"the unstyled resource should still be looked up; asked: " + finder.asked);
	}

	/**
	 * Every path the locator asks for stays within the package, for separators carried by the style.
	 */
	@Test
	void pathFromStyleStaysWithinPackage()
	{
		RecordingFinder finder = install(new RecordingFinder());

		tester.executeUrl(URL);

		assertFalse(finder.asked.stream().anyMatch(path -> path.contains("..")),
			"no path should contain a parent reference; asked: " + finder.asked);
		assertTrue(finder.asked.stream().allMatch(path -> path.startsWith(PACKAGE_PREFIX)),
			"every path should start with " + PACKAGE_PREFIX + "; asked: " + finder.asked);
	}

	/**
	 * As {@link #pathFromStyleStaysWithinPackage()}, for the locale: {@code ResourceUtil#parseLocale}
	 * hands its input to {@code Locale.of}, which does not validate it, and
	 * {@code LocaleResourceNameIterator} then appends {@code Locale#toString()} to the path. The
	 * locale may contain neither {@code -} (the attribute separator) nor {@code _} (the locale
	 * separator), and is lowercased by {@code parseLocale}.
	 */
	@Test
	void pathFromLocaleStaysWithinPackage()
	{
		RecordingFinder finder = install(new RecordingFinder());

		tester.executeUrl("wicket/resource/" +
			ResourceUrlAttributeValidationTest.class.getName() + "/PublicPage.html?" +
			"..%2F".repeat(PARENT_STEPS) + "otherpackage%2Fconfig");

		assertFalse(finder.asked.stream().anyMatch(path -> path.contains("..")),
			"no path should contain a parent reference; asked: " + finder.asked);
		assertTrue(finder.asked.stream().allMatch(path -> path.startsWith(PACKAGE_PREFIX)),
			"every path should start with " + PACKAGE_PREFIX + "; asked: " + finder.asked);
	}

	/**
	 * On a finder that collapses {@code ..} the way a container does, a path built from a style
	 * carrying separators would resolve outside the package. Nothing found there may be returned to
	 * the client.
	 */
	@Test
	void resourceResolvingOutsideThePackageIsNotServed()
	{
		install(new NormalizingFinder());

		tester.executeUrl(URL);

		assertFalse(tester.getLastResponseAsString().contains(OUTSIDE_CONTENT),
			"a resource resolved outside the package must not be served");
	}
}
