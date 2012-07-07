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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import org.apache.wicket.util.string.Strings;

class JavaLicenseHeaderHandler extends AbstractLicenseHeaderHandler
{
	private final Pattern javaHeaderPattern = Pattern.compile("^(.*?)package.*$",
		Pattern.MULTILINE | Pattern.DOTALL);

	/**
	 * Construct.
	 * 
	 * @param ignoreFiles
	 */
	public JavaLicenseHeaderHandler(final List<String> ignoreFiles)
	{
		super(ignoreFiles);
	}

	@Override
	public boolean addLicenseHeader(final File file)
	{
		boolean added = false;

		try
		{
			String fileContent = new org.apache.wicket.util.file.File(file).readString();

			Matcher mat = javaHeaderPattern.matcher(fileContent);
			if (mat.matches())
			{
				String header = mat.group(1);
				if (header.equals(getLicenseHeader()) == false)
				{
					String newContent = Strings.replaceAll(fileContent, header, "").toString();
					newContent = getLicenseHeader().trim() + LINE_ENDING + newContent;
					new org.apache.wicket.util.file.File(file).write(newContent);

					added = true;
				}
			}
			else
			{
				Assert.fail();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		return added;
	}

	@Override
	public boolean checkLicenseHeader(final File file)
	{
		String header = extractLicenseHeader(file, 0, 16);

		return getLicenseHeader().equals(header);
	}

	@Override
	public List<String> getSuffixes()
	{
		return Arrays.asList("java");
	}

	@Override
	protected String getLicenseHeaderFilename()
	{
		return "javaLicense.txt";
	}

	@Override
	public String getLicenseType(final File file)
	{
		String licenseType = null;

		String header = extractLicenseHeader(file, 0, 20);

		// Check for some of the known license types:
		if (header.contains("Apache License, Version 2.0"))
		{
			licenseType = "ASL2";
		}
		else if (header.contains("The Apache Software License, Version 1.1"))
		{
			licenseType = "ASL1.1";
		}

		return licenseType;
	}

}
