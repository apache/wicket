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
package org.apache.wicket.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

/**
 * Base class for testing exceptions in order to make sure that they achieve 100% test coverage.
 * Assumes that exceptions implement all of the four standard consturctors.
 * 
 * @author Chris Turner
 */
public abstract class ExceptionTestBase
{

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
	@Test
	final void emptyConstructor() throws Exception
	{
		Class<?> c = Class.forName(getExceptionClassName());
		Constructor<?> constructor = c.getConstructor((Class[])null);
		Exception e = (Exception)constructor.newInstance((Object[])null);
		assertNotNull(e, "Exception should be created");
		assertNull(e.getMessage());
		assertNull(e.getCause());
	}

	/**
	 * Test the string message constructor.
	 * 
	 * @throws Exception
	 *             If test fails
	 */
	@Test
	final void messageConstructor() throws Exception
	{
		Class<?> c = Class.forName(getExceptionClassName());
		Constructor<?> constructor = c.getConstructor(new Class[] { String.class });
		Exception e = (Exception)constructor.newInstance("test message");
		assertNotNull(e, "Exception should be created");
		assertEquals("test message", e.getMessage());
		assertNull(e.getCause());
	}

	/**
	 * Test the cause constructor.
	 * 
	 * @throws Exception
	 *             If test fails
	 */
	@Test
	final void causeConstructor() throws Exception
	{
		NullPointerException npe = new NullPointerException();
		Class<?> c = Class.forName(getExceptionClassName());
		Constructor<?> constructor = c.getConstructor(new Class[] { Throwable.class });
		Exception e = (Exception)constructor.newInstance(npe);
		assertNotNull(e, "Exception should be created");
		assertSame(npe, e.getCause());
	}

	/**
	 * Test the message and cause constructor.
	 * 
	 * @throws Exception
	 *             If test fails
	 */
	@Test
	final void messageAndCauseConstructor() throws Exception
	{
		NullPointerException npe = new NullPointerException();
		Class<?> c = Class.forName(getExceptionClassName());
		Constructor<?> constructor = c.getConstructor(new Class[] { String.class, Throwable.class });
		Exception e = (Exception)constructor.newInstance("test message", npe);
		assertNotNull(e, "Exception should be created");
		assertEquals("test message", e.getMessage());
		assertSame(npe, e.getCause());
	}

}
