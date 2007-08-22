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
package org.apache.wicket.protocol.http;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

public class WicketFilterTest extends TestCase
{
	public void testFilterPath1() {
			InputStream in = WicketFilterTest.class.getResourceAsStream("web1.xml");
			String filterPath = getFilterPath("FilterTestApplication", in);
			assertEquals("filtertest/", filterPath);
	}

	public void testFilterPath2() {
		InputStream in = WicketFilterTest.class.getResourceAsStream("web2.xml");
		String filterPath = getFilterPath("FilterTestApplication", in);
		assertEquals("filtertest/", filterPath);
}

	private String getFilterPath(String string, InputStream in)
	{
		try
		{
			Method method = WicketFilter.class.getDeclaredMethod("getFilterPath", new Class[] {String.class, InputStream.class});
			method.setAccessible(true);
			return method.invoke(new WicketFilter(), new Object[] {string, in}).toString();
		}
		catch (SecurityException e)
		{
			throw new RuntimeException(e);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}
}
