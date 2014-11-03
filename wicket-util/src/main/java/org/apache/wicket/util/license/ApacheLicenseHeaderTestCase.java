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
package org.apache.wicket.util.license;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.Strings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testcase used in the different wicket projects for testing for the correct ASL license headers.
 * Doesn't really make sense outside org.apache.wicket.
 * 
 * @author Frank Bille Jensen (frankbille)
 */
public abstract class ApacheLicenseHeaderTestCase extends Assert
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(ApacheLicenseHeaderTestCase.class);

	private static final String LINE_ENDING = System.getProperty("line.separator");

	static interface FileVisitor
	{
		/**
		 * @param file
		 */
		void visitFile(File file);
	}

	private class SuffixAndIgnoreFileFilter implements FileFilter
	{
		private final List<String> suffixes;
		private final List<String> ignoreFiles;

		private SuffixAndIgnoreFileFilter(final List<String> suffixes,
			final List<String> ignoreFiles)
		{
			this.suffixes = suffixes;
			this.ignoreFiles = ignoreFiles;
		}

		@Override
		public boolean accept(final File pathname)
		{
			boolean accept = false;

			if (pathname.isFile())
			{
				if (ignoreFile(pathname) == false)
				{
					for (String suffix : suffixes)
					{
						if (pathname.getName().endsWith("." + suffix))
						{
							accept = true;
							break;
						}
						else
						{
							log.info("File ignored: '{}'", pathname.toString());
						}
					}
				}
				else
				{
					log.info("File ignored: '{}'", pathname.toString());
				}
			}

			return accept;
		}

		private boolean ignoreFile(final File pathname)
		{
			boolean ignore = false;

			if (ignoreFiles != null)
			{
				String relativePathname = pathname.getAbsolutePath();
				relativePathname = Strings.replaceAll(relativePathname,
					baseDirectory.getAbsolutePath() + System.getProperty("file.separator"), "")
					.toString();

				for (String ignorePath : ignoreFiles)
				{
					// Will convert '/'s to '\\'s on Windows
					ignorePath = Strings.replaceAll(ignorePath, "/",
						System.getProperty("file.separator")).toString();
					File ignoreFile = new File(baseDirectory, ignorePath);

					// Directory ignore
					if (ignoreFile.isDirectory())
					{
						if (pathname.getAbsolutePath().startsWith(ignoreFile.getAbsolutePath()))
						{
							ignore = true;
							break;
						}
					}
					// Absolute file
					else if (ignoreFile.isFile())
					{
						if (relativePathname.equals(ignorePath))
						{
							ignore = true;
							break;
						}
					}
					else if (pathname.getName().equals(ignorePath))
					{
						ignore = true;
						break;
					}
				}
			}

			return ignore;
		}
	}

	private class DirectoryFileFilter implements FileFilter
	{
		private final String[] ignoreDirectory = new String[] { ".svn" };

		@Override
		public boolean accept(final File pathname)
		{
			boolean accept = false;

			if (pathname.isDirectory())
			{
				String relativePathname = pathname.getAbsolutePath();
				relativePathname = Strings.replaceAll(relativePathname,
					baseDirectory.getAbsolutePath() + System.getProperty("file.separator"), "")
					.toString();
				if (relativePathname.equals("target") == false)
				{
					boolean found = false;
					for (String ignore : ignoreDirectory)
					{
						if (pathname.getName().equals(ignore))
						{
							found = true;
							break;
						}
					}
					if (found == false)
					{
						accept = true;
					}
				}
			}

			return accept;
		}
	}

	private ILicenseHeaderHandler[] licenseHeaderHandlers;

	private File baseDirectory = new File("").getAbsoluteFile();

	protected List<String> javaIgnore = Generics.newArrayList();
	protected List<String> htmlIgnore = Generics.newArrayList();
	protected List<String> xmlPrologIgnore = Generics.newArrayList();
	protected List<String> propertiesIgnore = Generics.newArrayList();
	protected List<String> xmlIgnore = Generics.newArrayList();
	protected List<String> cssIgnore = Generics.newArrayList();
	protected List<String> velocityIgnore = Generics.newArrayList();
	protected List<String> javaScriptIgnore = Generics.newArrayList();
	protected boolean addHeaders = false;

	/**
	 * Construct.
	 */
	public ApacheLicenseHeaderTestCase()
	{

		// -------------------------------
		// Configure defaults
		// -------------------------------

		// addHeaders = true;
		xmlIgnore.add(".settings");
		xmlIgnore.add("EclipseCodeFormat.xml");
		xmlIgnore.add("nb-configuration.xml");

		/*
		 * License header in test files lower the visibility of the test.
		 */
		htmlIgnore.add("src/test/java");

		/*
		 * Low level configuration files for logging. No license needed.
		 */
		propertiesIgnore.add("src/test/java");

		/*
		 * .html in test is very test specific and a license header would confuse and make it
		 * unclear what the test is about.
		 */
		xmlPrologIgnore.add("src/test/java");

		/*
		 * Ignore package.html
		 */
		xmlPrologIgnore.add("package.html");
	}

	/**
	 * 
	 */
	@Before
	public final void before()
	{
		// setup the base directory for when running inside maven (building a release
		// comes to mind).
		String property = System.getProperty("basedir");
		if (!Strings.isEmpty(property))
		{
			baseDirectory = new File(property).getAbsoluteFile();
		}
	}

	/**
	 * Test all the files in the project which has an associated {@link ILicenseHeaderHandler}.
	 */
	@Test
	public void licenseHeaders()
	{
		licenseHeaderHandlers = new ILicenseHeaderHandler[] {
				new JavaLicenseHeaderHandler(javaIgnore),
				new JavaScriptLicenseHeaderHandler(javaScriptIgnore),
				new XmlLicenseHeaderHandler(xmlIgnore),
				new PropertiesLicenseHeaderHandler(propertiesIgnore),
				new HtmlLicenseHeaderHandler(htmlIgnore),
				new VelocityLicenseHeaderHandler(velocityIgnore),
				new XmlPrologHeaderHandler(xmlPrologIgnore),
				new CssLicenseHeaderHandler(cssIgnore), };

		final Map<ILicenseHeaderHandler, List<File>> badFiles = new HashMap<>();

		for (final ILicenseHeaderHandler licenseHeaderHandler : licenseHeaderHandlers)
		{
			visitFiles(licenseHeaderHandler.getSuffixes(), licenseHeaderHandler.getIgnoreFiles(),
				new FileVisitor()
				{
					@Override
					public void visitFile(final File file)
					{
						if (licenseHeaderHandler.checkLicenseHeader(file) == false)
						{
							if ((addHeaders == false) ||
								(licenseHeaderHandler.addLicenseHeader(file) == false))
							{
								List<File> files = badFiles.get(licenseHeaderHandler);

								if (files == null)
								{
									files = new ArrayList<>();
									badFiles.put(licenseHeaderHandler, files);
								}

								files.add(file);
							}
						}
					}
				});
		}

		failIncorrectLicenceHeaders(badFiles);
	}

	private void failIncorrectLicenceHeaders(final Map<ILicenseHeaderHandler, List<File>> files)
	{
		if (files.size() > 0)
		{
			StringBuilder failString = new StringBuilder();

			for (Entry<ILicenseHeaderHandler, List<File>> entry : files.entrySet())
			{
				ILicenseHeaderHandler licenseHeaderHandler = entry.getKey();
				List<File> fileList = entry.getValue();

				failString.append("\n");
				failString.append(licenseHeaderHandler.getClass().getName());
				failString.append(" failed. The following files(");
				failString.append(fileList.size());
				failString.append(") didn't have correct license header:\n");

				for (File file : fileList)
				{
					String filename = file.getAbsolutePath();

					// Find the license type
					String licenseType = licenseHeaderHandler.getLicenseType(file);

					if (licenseType == null)
					{
						failString.append("NONE");
					}
					else
					{
						failString.append(licenseType);
					}

					failString.append(" ").append(filename).append(LINE_ENDING);
				}
			}

			System.out.println(failString);
			fail(failString.toString());
		}
	}

	private void visitFiles(final List<String> suffixes, final List<String> ignoreFiles,
		final FileVisitor fileVisitor)
	{
		visitDirectory(suffixes, ignoreFiles, baseDirectory, fileVisitor);
	}

	private void visitDirectory(final List<String> suffixes, final List<String> ignoreFiles,
		final File directory, final FileVisitor fileVisitor)
	{
		File[] files = directory.listFiles(new SuffixAndIgnoreFileFilter(suffixes, ignoreFiles));

		if (files != null)
		{
			for (File file : files)
			{
				fileVisitor.visitFile(file);
			}
		}

		// Find the directories in this directory on traverse deeper
		files = directory.listFiles(new DirectoryFileFilter());

		if (files != null)
		{
			for (File childDirectory : files)
			{
				visitDirectory(suffixes, ignoreFiles, childDirectory, fileVisitor);
			}
		}
	}
}
