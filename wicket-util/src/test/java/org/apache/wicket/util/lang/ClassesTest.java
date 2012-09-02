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
package org.apache.wicket.util.lang;

import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;

/**
 * @since 1.5.8
 */
public class ClassesTest extends Assert
{
	@Test
	public void simpleName() throws Exception
	{

		assertEquals("String", Classes.simpleName(String.class));
		assertEquals("String", Classes.simpleName("".getClass()));

		assertEquals("SimpleDateFormat", Classes.simpleName(new SimpleDateFormat() {}.getClass()));

		// anonymous interface impl
		assertEquals("Object", Classes.simpleName(new Cloneable() {}.getClass()));
	}

	@Test
	public void name() throws Exception
	{

		assertEquals("java.lang.String", Classes.name(String.class));
		assertEquals("java.lang.String", Classes.name("".getClass()));

		assertEquals("java.text.SimpleDateFormat", Classes.name(new SimpleDateFormat() {}.getClass()));

		// anonymous interface impl
		assertEquals("java.lang.Object", Classes.name(new Cloneable() {}.getClass()));
	}
}
