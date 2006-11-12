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
package wicket.markup.html;

import junit.framework.TestCase;
import wicket.Application;
import wicket.SharedResources;
import wicket.util.tester.WicketTester;

/**
 * Tests for package resources.
 * 
 * @author Eelco Hillenius
 */
public class PackageResourceTest extends TestCase
{
	/** mock tester object */
	public WicketTester tester;

	/**
	 * Construct.
	 */
	public PackageResourceTest()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public PackageResourceTest(String name)
	{
		super(name);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		tester = new WicketTester();
	}

	/**
	 * Tests binding a single absolute package resource.
	 * 
	 * @throws Exception
	 */
	public void testBindAbsolutePackageResource() throws Exception
	{
		final SharedResources sharedResources = Application.get().getSharedResources();
		PackageResource.bind(tester.getApplication(), PackageResourceTest.class, "packaged1.txt");
		assertNotNull("resource packaged1.txt should be available as a packaged resource",
				sharedResources.get(PackageResourceTest.class, "packaged1.txt", null, null, true));
		assertNull("resource packaged2.txt should NOT be available as a packaged resource",
				sharedResources.get(PackageResourceTest.class, "packaged2.txt", null, null, true));
	}

	/**
	 * Tests {@link PackageResourceGuard}.
	 * 
	 * @throws Exception
	 */
	public void testPackageResourceGuard() throws Exception
	{
		PackageResourceGuard guard = new PackageResourceGuard();
		assertTrue(guard.acceptExtension("txt"));
		assertFalse(guard.acceptExtension("java"));
		assertTrue(guard.acceptAbsolutePath("foo/Bar.txt"));
		assertFalse(guard.acceptAbsolutePath("foo/Bar.java"));
		assertTrue(guard.accept(PackageResourceTest.class, "Bar.txt"));
		assertTrue(guard.accept(PackageResourceTest.class, "Bar.txt."));
		assertTrue(guard.accept(PackageResourceTest.class, ".Bar.txt"));
		assertTrue(guard.accept(PackageResourceTest.class, ".Bar.txt."));
		assertTrue(guard.accept(PackageResourceTest.class, ".Bar"));
		assertTrue(guard.accept(PackageResourceTest.class, ".java"));
		assertFalse(guard.accept(PackageResourceTest.class, "Bar.java"));
	}
}
