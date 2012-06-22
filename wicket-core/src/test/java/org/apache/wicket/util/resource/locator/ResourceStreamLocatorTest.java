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
package org.apache.wicket.util.resource.locator;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.wicket.util.file.Path;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.resource.IResourceStream;
import org.junit.Test;

public class ResourceStreamLocatorTest
{
	@Test
	public void defaultPath() throws Exception
	{
		IResourceStream stream = new ResourceStreamLocator(new Path()).locate(
			ResourceStreamLocatorTest.class,
			Packages.absolutePath(ResourceStreamLocatorTest.class, "foo.txt"));
		assertEquals("no prefix",
			new BufferedReader(new InputStreamReader(stream.getInputStream())).readLine());
	}

	@Test
	public void addedPath() throws Exception
	{
		final ResourceStreamLocator rsl = new ResourceStreamLocator(new Path());
		rsl.getClasspathLocationPrefixes().add("some/invalid/path");
		IResourceStream stream = rsl.locate(ResourceStreamLocatorTest.class,
			Packages.absolutePath(ResourceStreamLocatorTest.class, "foo.txt"));
		assertEquals("default path is first and should have had priority!", "no prefix",
			new BufferedReader(new InputStreamReader(stream.getInputStream())).readLine());
	}

	@Test
	public void replacedPathWithTrailingSlash() throws Exception
	{
		final ResourceStreamLocator rsl = new ResourceStreamLocator(new Path());
		rsl.getClasspathLocationPrefixes().clear();
		rsl.getClasspathLocationPrefixes().add("ResourceStreamLocatorTest/some/path/");
		IResourceStream stream = rsl.locate(ResourceStreamLocatorTest.class,
			Packages.absolutePath(ResourceStreamLocatorTest.class, "foo.txt"));
		assertEquals("should have found the version with prefix!", "with prefix",
			new BufferedReader(new InputStreamReader(stream.getInputStream())).readLine());
	}

	@Test
	public void replacedPathWithoutTrailingSlash() throws Exception
	{
		final ResourceStreamLocator rsl = new ResourceStreamLocator(new Path());
		rsl.getClasspathLocationPrefixes().clear();
		rsl.getClasspathLocationPrefixes().add("ResourceStreamLocatorTest/some/path");
		IResourceStream stream = rsl.locate(ResourceStreamLocatorTest.class,
			Packages.absolutePath(ResourceStreamLocatorTest.class, "foo.txt"));
		assertEquals("should have found the version with prefix!", "with prefix",
			new BufferedReader(new InputStreamReader(stream.getInputStream())).readLine());
	}
}
