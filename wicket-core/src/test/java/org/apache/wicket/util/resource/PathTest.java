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
package org.apache.wicket.util.resource;

import static org.apache.wicket.util.resource.ResourceStreamLocatorTest.*;

import java.io.File;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.util.file.Path;
import org.junit.Test;

public class PathTest extends WicketTestCase
{
	private static final String PACKAGE_PATH = PathTest.class.getPackage()
		.getName()
		.replace('.', File.separatorChar);
	private static final String CLASSPATH_ROOT = PathTest.class.getResource("/").getFile();

	private static final String FILENAME = ResourceStreamLocatorTest.class.getSimpleName() + ".txt";

	@Test
	public void loadFromRootUsingSubpathInFilename() throws Exception
	{
		Path path = new Path();
		path.add(CLASSPATH_ROOT);
		IResourceStream rs = path.find(PathTest.class, PACKAGE_PATH + File.separatorChar + FILENAME);
		assertNotNull(rs);
		assertEquals(FILENAME, getFilename(rs));
	}

	@Test
	public void loadFilenameFromPath() throws Exception
	{
		Path path = new Path();
		path.add(CLASSPATH_ROOT + File.separatorChar + PACKAGE_PATH);
		IResourceStream rs = path.find(PathTest.class, FILENAME);
		assertNotNull(rs);
		assertEquals(FILENAME, getFilename(rs));
	}
}
