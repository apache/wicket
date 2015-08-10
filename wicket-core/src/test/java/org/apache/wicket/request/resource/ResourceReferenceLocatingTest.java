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

import static org.hamcrest.CoreMatchers.is;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test case for WICKET-5967 - Unable to load i18n minified js.
 */
@SuppressWarnings("javadoc")
@RunWith(Parameterized.class)
public class ResourceReferenceLocatingTest extends WicketTestCase
{
	/** Loads the test cases from a spread sheet in semi-colon separated format. */
	@Parameters(name = "{0}")
	public static List<Object[]> parameters()
	{
		try (InputStream is = ResourceReferenceLocatingTest.class
			.getResourceAsStream("ResourceReferenceLocatingTest.csv"))
		{
			String csv = Streams.readString(is);
			StringReader sr = new StringReader(csv);
			BufferedReader br = new BufferedReader(sr);
			List<Object[]> result = new ArrayList<>();

			String line = br.readLine(); // read header line
			while ((line = br.readLine()) != null)
			{
				if (!line.isEmpty())
					result.add(new Object[] { TestCase.fromLine(line) });
			}
			return result;
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	public static class TestCase
	{
		private Locale locale;
		private String style;
		private String variation;
		private String extension;
		private String nonMinifiedContents;
		private String minifiedContents;
		private String strictNonMinifiedContents;
		private String strictMinifiedContents;

		private TestCase(Locale locale, String style, String variation, String extension,
			String nonMinifiedContents, String minifiedContents, String strictNonMinifiedContents,
			String strictMinifiedContents)
		{
			this.locale = locale;
			this.style = style;
			this.variation = variation;
			this.extension = extension;
			this.nonMinifiedContents = nonMinifiedContents;
			this.minifiedContents = minifiedContents;
			this.strictNonMinifiedContents = strictNonMinifiedContents;
			this.strictMinifiedContents = strictMinifiedContents;
		}

		private static boolean isNull(String s)
		{
			return s == null || s.isEmpty() || s.equals("null");
		}

		private static String nullOrValue(String s)
		{
			return isNull(s) ? null : s;
		}

		public static TestCase fromLine(String line)
		{
			String splitter;
			if (line.contains("\t"))
				splitter = "\t";
			else if (line.contains(";"))
				splitter = ";";
			else if (line.contains(","))
				splitter = ",";
			else
				throw new IllegalArgumentException(
					"Unable to split line with either tab, komma or semicolon");

			String[] pars = line.split(splitter);

			Locale locale = isNull(pars[0]) ? null
				: Locale.forLanguageTag(pars[0].replace('_', '-'));
			String style = nullOrValue(pars[1]);
			String variation = nullOrValue(pars[2]);
			String extension = nullOrValue(pars[3]);
			String resultNonMin = nullOrValue(pars[4]);
			String resultMin = nullOrValue(pars[5]);
			String resultStrictNonMin = nullOrValue(pars[6]);
			String resultStrictMin = nullOrValue(pars[7]);

			TestCase test = new TestCase(locale, style, variation, extension, resultNonMin,
				resultMin, resultStrictNonMin, resultStrictMin);
			return test;
		}

		public Locale getLocale()
		{
			return locale;
		}

		public String getStyle()
		{
			return style;
		}

		public String getVariation()
		{
			return variation;
		}

		public String getExtension()
		{
			return extension;
		}

		public String getNonMinifiedContents()
		{
			return nonMinifiedContents;
		}

		public String getMinifiedContents()
		{
			return minifiedContents;
		}

		public String getStrictNonMinifiedContents()
		{
			return strictNonMinifiedContents;
		}

		public String getStrictMinifiedContents()
		{
			return strictMinifiedContents;
		}

		@Override
		public String toString()
		{
			return "TestCase [locale=" + locale + ", style=" + style + ", variation=" + variation +
				", extension=" + extension + "]";
		}
	}

	private TestCase test;

	public ResourceReferenceLocatingTest(TestCase test)
	{
		this.test = test;
	}

	/**
	 * Locate a resource without a minification requirement.
	 */
	@Test
	public void locateNonMinifiedJavaScriptResourceReference()
	{
		tester.getApplication().getResourceSettings().setUseMinifiedResources(false);

		checkNonStrictUsingJavaScriptResourceReference(test.getNonMinifiedContents());
	}

	/** */
	@Test
	public void locateNonMinifiedPackageResourceReference()
	{
		tester.getApplication().getResourceSettings().setUseMinifiedResources(false);

		checkNonStrictUsingPackageResourceReference(test.getNonMinifiedContents());
	}

	/** */
	@Test
	public void locateMinifiedJavaScriptResourceReference()
	{
		tester.getApplication().getResourceSettings().setUseMinifiedResources(true);

		checkNonStrictUsingJavaScriptResourceReference(test.getMinifiedContents());
	}

	/** */
	@Test
	public void locateMinifiedPackageResourceReference()
	{
		tester.getApplication().getResourceSettings().setUseMinifiedResources(true);

		checkNonStrictUsingPackageResourceReference(test.getMinifiedContents());
	}

	private void checkNonStrictUsingJavaScriptResourceReference(String expectedResult)
	{
		Locale locale = test.getLocale();
		String style = test.getStyle();
		String variation = test.getVariation();
		String extension = test.getExtension();

		JavaScriptResourceReference reference = new JavaScriptResourceReference(
			ResourceReferenceLocatingTest.class, "b.js", locale, style, variation);
		tester.startResourceReference(reference);
		assertThat(test.toString(), tester.getLastResponseAsString().trim(),
			is("// " + expectedResult));
	}

	private void checkNonStrictUsingPackageResourceReference(String expectedResult)
	{
		Locale locale = test.getLocale();
		String style = test.getStyle();
		String variation = test.getVariation();
		String extension = test.getExtension();

		PackageResourceReference reference = new PackageResourceReference(
			ResourceReferenceLocatingTest.class, "b.js", locale, style, variation);
		tester.startResourceReference(reference);
		assertThat(test.toString(), tester.getLastResponseAsString().trim(),
			is("// " + expectedResult));
	}
}
