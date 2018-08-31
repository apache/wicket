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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * @author Juergen Donnerstag
 */
class PackageResourceGuardTest extends WicketTestCase
{
	/**
	 * Test acceptance of root folder.
	 */
	@Test
	void accept()
	{
		PackageResourceGuard guard = new PackageResourceGuard();

		guard.setAllowAccessToRootResources(false);
		assertFalse(guard.accept("test.gif"));

		guard.setAllowAccessToRootResources(true);
		assertTrue(guard.accept("test.gif"));


	}

	/**
	 * Test whether Windows absolute paths are handled properly on the current system (properly
	 * works on Windows and properly blocks on any other OS).
	 */
	@Test
	void acceptAbsolutePath()
	{
		PackageResourceGuard guard = new PackageResourceGuard();
		guard.setAllowAccessToRootResources(false);

		assertTrue(guard.accept("/test/test.js"));
		assertTrue(guard.accept("/test/.java"));
		assertFalse(guard.accept("/test.js"));

		if ("\\".equals(File.separator))
		{
			assertTrue(guard.accept("c:\\test\\org\\apache\\.java"));
			assertTrue(guard.accept("\\test\\org\\apache\\.java"));
			assertTrue(guard.accept("c:\\test\\org\\apache\\test.js"));
			assertTrue(guard.accept("\\test\\org\\apache\\test.js"));
			assertFalse(guard.accept("c:\\test.js"));
			assertFalse(guard.accept("\\test.js"));

			// java also generates file paths with '/' on windows
			assertTrue(guard.accept("c:/test/org/apache/.java"));
			assertTrue(guard.accept("/test/org/apache/.java"));
			assertTrue(guard.accept("c:/test/org/apache/test.js"));
			assertTrue(guard.accept("/test/org/apache/test.js"));
			assertFalse(guard.accept("c:/test.js"));
			assertFalse(guard.accept("/test.js"));
		}
	}

	/**
	 * Allow access to non-component markup
	 * 
	 * @throws Exception
	 */
	@Test
	void markup() throws Exception
	{
		PackageResourceGuard guard = new PackageResourceGuard();

		assertNotNull(getClass().getResource(
			"/org/apache/wicket/markup/html/PackageResourceGuardTest$MyClass.class"));

		assertFalse(guard
			.accept("org/apache/wicket/markup/html/PackageResourceGuardTest$MyClass.html"));
		assertFalse(guard
			.accept("org/apache/wicket/markup/html/PackageResourceGuardTest$MyClass_de.html"));
		assertFalse(guard
			.accept("org/apache/wicket/markup/html/PackageResourceGuardTest$MyClass_SomeHTMLSnippetIWantServed.html"));
		assertFalse(guard
			.accept("org/apache/wicket/markup/html/PackageResourceGuardTest$MyOtherClass_WithCrazyName.html"));
		assertFalse(guard
			.accept("org/apache/wicket/markup/html/PackageResourceGuardTest$MyOtherClass_WithCrazyName_de.html"));
		assertFalse(guard
			.accept("org/apache/wicket/markup/html/PackageResourceGuardTest$MyOtherClass_WithCrazyName_en.html"));
		assertTrue(guard
			.accept("org/apache/wicket/markup/html/PackageResourceGuardTest$MyOtherClass.html"));
	}

	private class MyClass extends WebComponent
	{

		public MyClass(String id)
		{
			super(id);
		}
	}

	private class MyOtherClass_WithCrazyName extends WebComponent
	{

		public MyOtherClass_WithCrazyName(String id)
		{
			super(id);
		}
	}
}
