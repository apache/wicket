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

import org.apache.wicket.Application;
import org.apache.wicket.WicketTestCase;

/**
 * @author Juergen Donnerstag
 */
public class SecurePackageResourceGuardTest extends WicketTestCase
{
	private SecurePackageResourceGuard newGuard()
	{
		SecurePackageResourceGuard guard = new SecurePackageResourceGuard();
		guard.getPattern().clear();
		return guard;
	}
	
	/**
	 * 
	 */
	public void test_accept()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+*.gif");
		assertTrue(guard.accept(Application.class, "test.gif"));
		assertTrue(guard.accept(Application.class, "mydir/test.gif"));
		assertTrue(guard.accept(Application.class, "/root/mydir/test.gif"));
		assertTrue(guard.accept(Application.class, "../test.gif"));
		assertTrue(guard.accept(Application.class, "../../test.gif"));
		assertTrue(guard.accept(Application.class, "../../../test.gif"));

		boolean hit = false;
		try
		{
			// you can not go below root
			assertTrue(guard.accept(Application.class, "../../../../test.gif"));
		}
		catch (IllegalArgumentException ex)
		{
			hit = true;
		}
		assertTrue("Expected an IllegalArgumentException", hit);
	}

	/**
	 * 
	 */
	public void test_acceptAbsolutePath()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+*.gif");
		assertTrue(guard.acceptAbsolutePath("test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/test.gif"));
		assertTrue(guard.acceptAbsolutePath("/root/mydir/test.gif"));
	}

	/**
	 * 
	 */
	public void test_fileOnly()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+**.gif");
		guard.addPattern("+*.gif*");
		guard.addPattern("+*.gi*");
		guard.addPattern("+test*.gif");

		assertTrue(guard.acceptAbsolutePath("test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/test.gif"));
		assertTrue(guard.acceptAbsolutePath("/root/mydir/test.gif"));

		// ".." are not allowed
		assertFalse(guard.acceptAbsolutePath("../test.gif"));

		assertTrue(guard.acceptAbsolutePath("test.giX"));
		assertTrue(guard.acceptAbsolutePath("mydir/test.gifABCD"));
		assertTrue(guard.acceptAbsolutePath("mydir/testXXX.gif"));

		guard.addPattern("-**/testA.gif");
		assertFalse(guard.acceptAbsolutePath("mydir/testA.gif"));
	}

	/**
	 * 
	 */
	public void test_withDirectory()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+mydir/*/*.gif");

		assertFalse(guard.acceptAbsolutePath("test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/test.gif"));
		assertFalse(guard.acceptAbsolutePath("/mydir/test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir2/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir2/dir3/xxx.gif"));
	}

	/**
	 * 
	 */
	public void test_1()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+mydir/**/*.gif");

		assertFalse(guard.acceptAbsolutePath("test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir2/xxx.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir2/dir3/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("/mydir/test.gif"));
	}

	/**
	 * 
	 */
	public void test_2()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+*my*dir*/*/*.gif");

		assertFalse(guard.acceptAbsolutePath("test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir2/xxx.gif"));
		assertTrue(guard.acceptAbsolutePath("mydirXX/dir2/xxx.gif"));
		assertTrue(guard.acceptAbsolutePath("AAmydirXX/dir2/xxx.gif"));
		assertTrue(guard.acceptAbsolutePath("myBBdirXX/dir2/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir2/dir3/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("/mydir/test.gif"));
	}

	/**
	 * 
	 */
	public void test_3()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+mydir**/*X/*.gif");

		assertFalse(guard.acceptAbsolutePath("test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir2/xxx.gif"));
		assertTrue(guard.acceptAbsolutePath("mydirAA/dir2X/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("mydirAA/dir2/xxx.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir2X/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir2/dir3/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("/mydir/test.gif"));
	}

	/**
	 * 
	 */
	public void test_4()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+mydir/**/xxx/**/*.gif");

		assertFalse(guard.acceptAbsolutePath("test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/xxx/test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir2/xxx/test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir2/xxx/yyy/test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir1/xxx/test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir1/dir2/xxx/test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir1/xxx/dir3/xxx.gif"));

		assertFalse(guard.acceptAbsolutePath("mydir/dir2/aaa/test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir2/aaa/yyy/test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir1/aaa/test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir1/dir2/aaa/test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir1/aaa/dir3/test.gif"));

		assertFalse(guard.acceptAbsolutePath("/mydir/test.gif"));
	}

	/**
	 * 
	 */
	public void test_5()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+/**/*.gif");

		assertFalse(guard.acceptAbsolutePath("test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir2/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir2/dir3/xxx.gif"));
		assertTrue(guard.acceptAbsolutePath("/mydir/test.gif"));
		assertTrue(guard.acceptAbsolutePath("/mydir/dir2/test.gif"));
	}

	/**
	 * 
	 */
	public void test_6()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+**/*.gif");

		assertTrue(guard.acceptAbsolutePath("test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir2/xxx.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/dir2/dir3/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("/mydir/test.gif"));
	}

	/**
	 * 
	 */
	public void test_7()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+*/*.gif");

		assertFalse(guard.acceptAbsolutePath("test.gif"));
		assertTrue(guard.acceptAbsolutePath("mydir/test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir2/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/dir2/dir3/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("/mydir/test.gif"));
	}

	/**
	 * 
	 */
	public void test_8()
	{
		SecurePackageResourceGuard guard = newGuard();
		guard.addPattern("+/*/*.gif");

		assertFalse(guard.acceptAbsolutePath("test.gif"));
		assertFalse(guard.acceptAbsolutePath("mydir/test.gif"));
		assertTrue(guard.acceptAbsolutePath("/mydir/test.gif"));
		assertFalse(guard.acceptAbsolutePath("/mydir/dir2/xxx.gif"));
		assertFalse(guard.acceptAbsolutePath("/mydir/dir2/dir3/xxx.gif"));
	}
}
