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
package wicket.util;

import java.lang.reflect.Constructor;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Base class for testing exceptions in order to make sure that they achieve
 * 100% test coverage. Assumes that exceptions implement all of the four
 * standard consturctors.
 * 
 * @author Chris Turner
 */
public abstract class ExceptionTestBase extends TestCase
{

	/**
	 * Create the test case.
	 * 
	 * @param s
	 *            The test name
	 */
	protected ExceptionTestBase(String s)
	{
		super(s);
	}

	/**
	 * Return the name of the exception class to be tested.
	 * 
	 * @return The name of the exception class
	 */
	protected abstract String getExceptionClassName();

	/**
	 * Test the no argument constructor.
	 * 
	 * @throws Exception
	 *             If test fails
	 */
	public final void testEmptyConstructor() throws Exception
	{
		Class c = Class.forName(getExceptionClassName());
		Constructor constructor = c.getConstructor((Class[])null);
		Exception e = (Exception)constructor.newInstance((Object[])null);
		Assert.assertNotNull("Exception should be created", e);
		Assert.assertNull(e.getMessage());
		Assert.assertNull(e.getCause());
	}

	/**
	 * Test the string message constructor.
	 * 
	 * @throws Exception
	 *             If test fails
	 */
	public final void testMessageConstructor() throws Exception
	{
		Class c = Class.forName(getExceptionClassName());
		Constructor constructor = c.getConstructor(new Class[] { String.class });
		Exception e = (Exception)constructor.newInstance(new Object[] { "test message" });
		Assert.assertNotNull("Exception should be created", e);
		Assert.assertEquals("test message", e.getMessage());
		Assert.assertNull(e.getCause());
	}

	/**
	 * Test the cause constructor.
	 * 
	 * @throws Exception
	 *             If test fails
	 */
	public final void testCauseConstructor() throws Exception
	{
		NullPointerException npe = new NullPointerException();
		Class c = Class.forName(getExceptionClassName());
		Constructor constructor = c.getConstructor(new Class[] { Throwable.class });
		Exception e = (Exception)constructor.newInstance(new Object[] { npe });
		Assert.assertNotNull("Exception should be created", e);
		Assert.assertSame(npe, e.getCause());
	}

	/**
	 * Test the message and cause constructor.
	 * 
	 * @throws Exception
	 *             If test fails
	 */
	public final void testMessageAndCauseConstructor() throws Exception
	{
		NullPointerException npe = new NullPointerException();
		Class c = Class.forName(getExceptionClassName());
		Constructor constructor = c.getConstructor(new Class[] { String.class, Throwable.class });
		Exception e = (Exception)constructor.newInstance(new Object[] { "test message", npe });
		Assert.assertNotNull("Exception should be created", e);
		Assert.assertEquals("test message", e.getMessage());
		Assert.assertSame(npe, e.getCause());
	}

}
