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

import junit.framework.Assert;
import org.apache.wicket.util.diff.Diff;
import org.apache.wicket.util.diff.Revision;

class VelocityLicenseHeaderHandler extends AbstractLicenseHeaderHandler
{
	/**
	 * Construct.
	 * 
	 * @param ignoreFiles
	 */
	public VelocityLicenseHeaderHandler(final List<String> ignoreFiles)
	{
		super(ignoreFiles);
	}

	@Override
	protected String getLicenseHeaderFilename()
	{
		return "velocityLicense.txt";
	}

	@Override
	public boolean checkLicenseHeader(final File file)
	{
		Revision revision = null;

		try
		{
			String header = extractLicenseHeader(file, 0, 16);

			revision = Diff.diff(getLicenseHeader().split(LINE_ENDING), header.split(LINE_ENDING));
		}
		catch (Exception e)
		{
			Assert.fail(e.getMessage());
		}

		return revision.size() == 0;
	}

	@Override
	public List<String> getSuffixes()
	{
		return Arrays.asList("vm");
	}

	@Override
	public boolean addLicenseHeader(final File file)
	{
		prependLicenseHeader(file);

		return true;
	}

}
