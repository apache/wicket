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
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import wicket.util.diff.Diff;
import wicket.util.diff.Revision;

/**
 * A silly try to create a testcase for running through all files in the project
 * and check if they have the correct license headers. Lets see if it holds.
 * 
 * @author Frank Bille Jensen (frankbille)
 */
public abstract class ApacheLicenseHeaderTestCase extends TestCase
{
	private static final String LINE_ENDING = System.getProperty("line.separator");

	private interface FileVisitor
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
				boolean ignore = false;
				
				if (ignoreFiles != null)
				{
					String relativePathname = pathname.getAbsolutePath();
					relativePathname = relativePathname.replace(baseDirectory.getAbsolutePath()+System.getProperty("file.separator"), "");
					
					for (String ignoreFile : ignoreFiles)
					{
						if (relativePathname.equals(ignoreFile))
						{
							ignore = true;
							break;
						}
					}
				}
				
				if (ignore == false)
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

	private File baseDirectory = new File("").getAbsoluteFile();
	private String javaLicenseHeader;
	private String xmlLicenseHeader;
	private String propertiesLicenseHeader;
	private String cssLicenseHeader;
	private String velocityLicenseHeader;
	private String javaScriptLicenseHeader;
	private Pattern xmlHeader = Pattern.compile(
			"^(\\<\\?xml version=\"1.0\" encoding=\"[^\"]+\"[ ]*\\?\\>" + LINE_ENDING + ").*",
			Pattern.DOTALL);

	/**
	 * Construct.
	 */
	public ApacheLicenseHeaderTestCase()
	{
		super("Test of the legal aspects of the Wicket source code is correct.");

		// Load licenses
		javaLicenseHeader = loadFile("javaLicense.txt");
		xmlLicenseHeader = loadFile("xmlLicense.txt");
		propertiesLicenseHeader = loadFile("propertiesLicense.txt");
		cssLicenseHeader = loadFile("cssLicense.txt");
		velocityLicenseHeader = loadFile("velocityLicense.txt");
		javaScriptLicenseHeader = loadFile("javaScriptLicense.txt");
	}

	/**
	 * Test all java files.
	 */
	public void testJavaFiles()
	{
		final List<File> badFiles = new ArrayList<File>();

		visitFiles("java", new FileVisitor()
		{
			public void visitFile(File file)
			{
				if (checkJavaHeader(file) == false)
				{
					badFiles.add(file);
				}
			}
		});

		failIncorrectLicenceHeaders(badFiles);
	}

	/**
	 * Test all html files.
	 */
	public void testHtmlFiles()
	{
		final List<File> badFiles = new ArrayList<File>();

		visitFiles("html", new FileVisitor()
		{
			public void visitFile(File file)
			{
				if (checkXmlHeader(file) == false)
				{
					badFiles.add(file);
				}
			}
		});

		failIncorrectLicenceHeaders(badFiles);
	}

	/**
	 * Test all properties files.
	 */
	public void testPropertiesFiles()
	{
		final List<File> badFiles = new ArrayList<File>();

		visitFiles("properties", new FileVisitor()
		{
			public void visitFile(File file)
			{
				if (checkPropertiesHeader(file) == false)
				{
					badFiles.add(file);
				}
			}
		});

		failIncorrectLicenceHeaders(badFiles);
	}

	/**
	 * Test all xml files.
	 */
	public void testXmlFiles()
	{
		final List<File> badFiles = new ArrayList<File>();

		visitFiles(new String[] { "xml", "fml" }, new FileVisitor()
		{
			public void visitFile(File file)
			{
				if (checkXmlHeader(file) == false)
				{
					badFiles.add(file);
				}
			}
		});

		failIncorrectLicenceHeaders(badFiles);
	}

	/**
	 * Test all velocity files.
	 */
	public void testVelocityFiles()
	{
		final List<File> badFiles = new ArrayList<File>();

		visitFiles("vm", new FileVisitor()
		{
			public void visitFile(File file)
			{
				if (checkVelocityHeader(file) == false)
				{
					badFiles.add(file);
				}
			}
		});

		failIncorrectLicenceHeaders(badFiles);
	}

	/**
	 * Test all javascript files.
	 */
	public void testJavaScriptFiles()
	{
		final List<File> badFiles = new ArrayList<File>();

		visitFiles("js", new FileVisitor()
		{
			public void visitFile(File file)
			{
				if (checkJavaScriptHeader(file) == false)
				{
					badFiles.add(file);
				}
			}
		});

		failIncorrectLicenceHeaders(badFiles);
	}

	private void failIncorrectLicenceHeaders(List<File> files)
	{
		if (files.size() > 0)
		{
			StringBuffer failString = new StringBuffer();

			failString.append("The following files didn't have a correct license header:\n");

			for (File file : files)
			{
				failString.append(file.getAbsolutePath()).append(LINE_ENDING);
			}

			fail(failString.toString());
		}
	}

	private String extractLicenseHeader(File file, int start, int length)
	{
		String header = "";
		FileReader fileReader = null;

		try
		{
			fileReader = new FileReader(file);
			LineNumberReader lineNumberReader = new LineNumberReader(fileReader);

			for (int i = start; i < length; i++)
			{
				header += lineNumberReader.readLine() + LINE_ENDING;
			}
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
		finally
		{
			if (fileReader != null)
			{
				try
				{
					fileReader.close();
				}
				catch (IOException e)
				{
					fail(e.getMessage());
				}
			}
		}

		return header.trim();
	}

	private boolean checkJavaHeader(File file)
	{
		String header = extractLicenseHeader(file, 0, 16);

		return javaLicenseHeader.equals(header);
	}

	private boolean checkJavaScriptHeader(File file)
	{
		String header = extractLicenseHeader(file, 0, 16);

		return javaScriptLicenseHeader.equals(header);
	}

	private boolean checkXmlHeader(File file)
	{
		Revision revision = null;

		try
		{
			String header = extractLicenseHeader(file, 0, 17);

			Matcher mat = xmlHeader.matcher(header);
			if (mat.matches())
			{
				header = header.replace(mat.group(1), "");
			}
			else
			{
				// Then only take the first 16 lines
				String[] headers = header.split(LINE_ENDING);
				header = "";
				for (int i = 0; i < 16; i++)
				{
					header += headers[i] + LINE_ENDING;
				}
			}

			revision = Diff.diff(xmlLicenseHeader.split(LINE_ENDING), header.split(LINE_ENDING));
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

		return revision.size() == 0;
	}

	private boolean checkPropertiesHeader(File file)
	{
		Revision revision = null;

		try
		{
			String header = extractLicenseHeader(file, 0, 14);

			revision = Diff.diff(propertiesLicenseHeader.split(LINE_ENDING), header
					.split(LINE_ENDING));
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

		return revision.size() == 0;
	}

	private boolean checkVelocityHeader(File file)
	{
		Revision revision = null;

		try
		{
			String header = extractLicenseHeader(file, 0, 16);

			revision = Diff.diff(velocityLicenseHeader.split(LINE_ENDING), header
					.split(LINE_ENDING));
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

		return revision.size() == 0;
	}

	private void visitFiles(String suffix, FileVisitor fileVisitor)
	{
		visitFiles(new String[] { suffix }, null, fileVisitor);
	}
	
	private void visitFiles(String suffix, String[] ignoreFiles, FileVisitor fileVisitor)
	{
		visitFiles(new String[] { suffix }, ignoreFiles, fileVisitor);
	}
	
	private void visitFiles(String[] suffixes, FileVisitor fileVisitor)
	{
		visitFiles(suffixes, null, fileVisitor);
	}

	private void visitFiles(String[] suffixes, String[] ignoreFiles, FileVisitor fileVisitor)
	{
		visitDirectory(suffixes, ignoreFiles, baseDirectory, fileVisitor);
	}

	private void visitDirectory(String[] suffixes, String[] ignoreFiles, File directory, FileVisitor fileVisitor)
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

	/**
	 * @param filename
	 * @return The contents of the file
	 */
	private String loadFile(String filename)
	{
		String contents = null;

		try
		{
			URL url = ApacheLicenseHeaderTestCase.class.getResource(".");
			File legalsDir = new File(url.toURI());
			String legalsDirString = legalsDir.getAbsolutePath();
			contents = new wicket.util.file.File(legalsDirString, filename).readString();
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

		return contents.trim();
	}
}
