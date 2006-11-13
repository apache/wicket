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
package wicket.util.license;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Testcase used in the different wicket projects for testing for the correct
 * ASL license headers. Doesn't really make sense outside wicket.
 * 
 * @author Frank Bille Jensen (frankbille)
 */
public abstract class ApacheLicenseHeaderTestCase extends TestCase
{
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
		private String[] suffixes;
		private String[] ignoreFiles;

		private SuffixAndIgnoreFileFilter(String[] suffixes)
		{
			this(suffixes, null);
		}

		private SuffixAndIgnoreFileFilter(String[] suffixes, String[] ignoreFiles)
		{
			this.suffixes = suffixes;
			this.ignoreFiles = ignoreFiles;
		}

		public boolean accept(File pathname)
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
					}
				}
			}

			return accept;
		}
		
		private boolean ignoreFile(File pathname)
		{
			boolean ignore = false;
			
			if (ignoreFiles != null)
			{
				String relativePathname = pathname.getAbsolutePath();
				relativePathname = relativePathname.replace(baseDirectory.getAbsolutePath()
						+ System.getProperty("file.separator"), "");

				for (String ignorePath : ignoreFiles)
				{
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
					if (ignoreFile.isFile())
					{
						if (relativePathname.equals(ignorePath))
						{
							ignore = true;
							break;
						}
					}
				}
			}
			
			return ignore;
		}
	}

	private class DirectoryFileFilter implements FileFilter
	{
		private String[] ignoreDirectory = new String[] { ".svn", "target" };

		public boolean accept(File pathname)
		{
			boolean accept = false;

			if (pathname.isDirectory())
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

			return accept;
		}
	}

	private ILicenseHeaderHandler[] licenseHeaderHandlers;
	
	private File baseDirectory = new File("").getAbsoluteFile();

	protected String[] javaIgnore;
	protected String[] htmlIgnore;
	protected String[] propertiesIgnore;
	protected String[] xmlIgnore;
	protected String[] cssIgnore;
	protected String[] velocityIgnore;
	protected String[] javaScriptIgnore;
	protected boolean addHeaders = false;

	/**
	 * Construct.
	 */
	public ApacheLicenseHeaderTestCase()
	{
		super("Test of the legal aspects of the Wicket source code is correct.");
	}
	
	/**
	 * Test all the files in the project which has an associated {@link ILicenseHeaderHandler}.
	 */
	public void testLicenseHeaders()
	{
		licenseHeaderHandlers = new ILicenseHeaderHandler[] {
			new JavaLicenseHeaderHandler(javaIgnore),
			new JavaScriptLicenseHeaderHandler(javaScriptIgnore),
			new XmlLicenseHeaderHandler(xmlIgnore),
			new PropertiesLicenseHeaderHandler(propertiesIgnore),
			new CssLicenseHeaderHandler(cssIgnore),
			new HtmlLicenseHeaderHandler(htmlIgnore),
			new VelocityLicenseHeaderHandler(velocityIgnore)
		};
		
		final Map<ILicenseHeaderHandler, List<File>> badFiles = new HashMap<ILicenseHeaderHandler, List<File>>();
		
		for (final ILicenseHeaderHandler licenseHeaderHandler : licenseHeaderHandlers)
		{
			visitFiles(licenseHeaderHandler.getSuffixes(), licenseHeaderHandler.getIgnoreFiles(), new FileVisitor()
			{
				public void visitFile(File file)
				{
					if (licenseHeaderHandler.checkLicenseHeader(file) == false)
					{
						if (addHeaders == false 
								|| licenseHeaderHandler.addLicenseHeader(file) == false)
						{
							List<File> files = badFiles.get(licenseHeaderHandler);
							
							if (files == null) 
							{
								files = new ArrayList<File>();
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

	private void failIncorrectLicenceHeaders(Map<ILicenseHeaderHandler, List<File>> files)
	{
		if (files.size() > 0)
		{
			StringBuffer failString = new StringBuffer();

			for (ILicenseHeaderHandler licenseHeaderHandler : files.keySet())
			{
				failString.append("\n");
				failString.append(licenseHeaderHandler.getClass().getName());
				failString.append(" failed. The following files(");
				failString.append(files.get(licenseHeaderHandler).size());
				failString.append(") didn't have correct license header:\n");
				
				for (File file : files.get(licenseHeaderHandler))
				{
					failString.append(file.getAbsolutePath()).append(LINE_ENDING);
				}
			}

			System.out.println(failString);
			fail(failString.toString());
		}
	}
	private void visitFiles(String[] suffixes, String[] ignoreFiles, FileVisitor fileVisitor)
	{
		visitDirectory(suffixes, ignoreFiles, baseDirectory, fileVisitor);
	}

	private void visitDirectory(String[] suffixes, String[] ignoreFiles, File directory,
			FileVisitor fileVisitor)
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
