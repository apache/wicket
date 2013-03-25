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
package org.apache.wicket.markup.html;

import java.io.File;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

/**
 * @author Juergen Donnerstag
 */
public class PackageResourceGuardTest extends WicketTestCase
{
	/**
	 * Test acceptance of root folder.
	 */
	@Test
	public void accept()
	{
		PackageResourceGuard guard = new PackageResourceGuard();

		guard.setAllowAccessToRootResources(false);
		assertFalse(guard.accept(Integer.TYPE, "test.gif"));

		guard.setAllowAccessToRootResources(true);
		assertTrue(guard.accept(Integer.TYPE, "test.gif"));


	}

	/**
	 * Test whether Windows absolute paths are handled properly on the current system (properly
	 * works on Windows and properly blocks on any other OS).
	 */
	@Test
	public void acceptAbsolutePath()
	{
		PackageResourceGuard guard = new PackageResourceGuard();
		guard.setAllowAccessToRootResources(false);

		assertTrue(guard.acceptAbsolutePath("/test/test.js"));
		assertFalse(guard.acceptAbsolutePath("/test.js"));

		if ("\\".equals(File.pathSeparator))
		{
			assertTrue(guard.acceptAbsolutePath("c:\\test\\org\\apache\\test.js"));
			assertTrue(guard.acceptAbsolutePath("\\test\\org\\apache\\test.js"));
			assertFalse(guard.acceptAbsolutePath("c:\\test.js"));
			assertFalse(guard.acceptAbsolutePath("\\test.js"));

			// java also generates file paths with '/' on windows
			assertTrue(guard.acceptAbsolutePath("c:/test/org/apache/test.js"));
			assertTrue(guard.acceptAbsolutePath("/test/org/apache/test.js"));
			assertFalse(guard.acceptAbsolutePath("c:/test.js"));
			assertFalse(guard.acceptAbsolutePath("/test.js"));
		}


	}

}
