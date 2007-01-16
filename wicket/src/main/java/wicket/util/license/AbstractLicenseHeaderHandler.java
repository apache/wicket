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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;

import junit.framework.Assert;
import wicket.util.io.Streams;
import wicket.util.string.Strings;


abstract class AbstractLicenseHeaderHandler implements ILicenseHeaderHandler
{
	protected static final String LINE_ENDING = System.getProperty("line.separator");
	private String licenseHeader;
	private String[] ignoreFiles;

	/**
	 * Construct.
	 * 
	 * @param ignoreFiles
	 */
	public AbstractLicenseHeaderHandler(String[] ignoreFiles)
	{
		this.ignoreFiles = ignoreFiles;
	}

	public String[] getIgnoreFiles()
	{
		return ignoreFiles;
	}

	public boolean addLicenseHeader(File file)
	{
		System.out.println("Not supported yet.");
		return false;
	}

	public String getLicenseType(File file)
	{
		return null;
	}

	protected abstract String getLicenseHeaderFilename();

	protected String getLicenseHeader()
	{
		if (Strings.isEmpty(licenseHeader))
		{
			try
			{
				InputStream in = ApacheLicenseHeaderTestCase.class.getResourceAsStream(getLicenseHeaderFilename());
				try
				{
					licenseHeader = Streams.readString(in);
				}
				finally
				{
					in.close();
				}
				licenseHeader = licenseHeader.trim();
			}
			catch (Exception e)
			{
				Assert.fail(e.getMessage());

			}
		}

		return licenseHeader;
	}

	protected String extractLicenseHeader(File file, int start, int length)
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
			Assert.fail(e.getMessage());
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
					Assert.fail(e.getMessage());
				}
			}
		}

		return header.trim();
	}

	/**
	 * Add the license header to the start of the file without caring about
	 * existing license headers.
	 * 
	 * @param file
	 *            The file to add the license header to.
	 */
	protected void prependLicenseHeader(File file)
	{
		try
		{
			String content = new wicket.util.file.File(file).readString();
			content = getLicenseHeader() + LINE_ENDING + content;
			new wicket.util.file.File(file).write(content);
		}
		catch (Exception e)
		{
			Assert.fail(e.getMessage());
		}
	}
}
