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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import wicket.util.diff.Diff;
import wicket.util.diff.Revision;
import wicket.util.string.Strings;

class XmlLicenseHeaderHandler extends AbstractLicenseHeaderHandler
{
	private Pattern xmlHeader = Pattern.compile("^(\\<\\?xml[^"+LINE_ENDING+"]+?)"+LINE_ENDING+"(.*)$", Pattern.DOTALL | Pattern.MULTILINE);
	
	/**
	 * Construct.
	 * 
	 * @param ignoreFiles
	 */
	public XmlLicenseHeaderHandler(String[] ignoreFiles)
	{
		super(ignoreFiles);
	}

	@Override
	protected String getLicenseHeaderFilename()
	{
		return "xmlLicense.txt";
	}

	public boolean checkLicenseHeader(File file)
	{
		Revision revision = null;

		try
		{
			String header = extractLicenseHeader(file, 0, 17);

			if (header.startsWith("<?xml"))
			{
				header = header.substring(header.indexOf(LINE_ENDING) + 1);
			}
			else
			{
				// Then only take the first 16 lines
				String[] headers = header.split(LINE_ENDING);
				header = "";
				for (int i = 0; i < 16 && i < headers.length; i++)
				{
					if (header.length() > 0)
					{
						header += LINE_ENDING;
					}
					header += headers[i];
				}
			}

			revision = Diff.diff(getLicenseHeader().split(LINE_ENDING), header.split(LINE_ENDING));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		return revision.size() == 0;
	}

	public String[] getSuffixes()
	{
		return new String[] { "xml", "fml" };
	}

	@Override
	public boolean addLicenseHeader(File file)
	{
		boolean added = false;
		
		try
		{
			String content = new wicket.util.file.File(file).readString();
			String xml = "";
			StringBuffer newContent = new StringBuffer();
			
			Matcher mat = xmlHeader.matcher(content);
			if (mat.matches())
			{
				xml = mat.group(1);
				content = mat.group(2);
			}
			
			if (Strings.isEmpty(xml) == false)
			{
				newContent.append(xml).append(LINE_ENDING);
			}
			
			newContent.append(getLicenseHeader()).append(LINE_ENDING);
			newContent.append(content);
			
			new wicket.util.file.File(file).write(newContent.toString());
			added = true;
		}
		catch (Exception e)
		{
			Assert.fail(e.getMessage());
		}
		
		return added;
	}

	
}
