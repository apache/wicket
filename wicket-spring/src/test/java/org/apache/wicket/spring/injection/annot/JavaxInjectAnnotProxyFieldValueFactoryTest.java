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
package org.apache.wicket.spring.injection.annot;

import java.lang.reflect.Field;

import org.apache.wicket.spring.injection.util.JavaxInjectInjectable;
import org.junit.Test;


/**
 * Tests for Spring injection with {@literal @javax.inject.Inject} annotation
 */
public class JavaxInjectAnnotProxyFieldValueFactoryTest extends AnnotProxyFieldValueFactoryTest
{
	/**
	 * Construct.
	 */
	public JavaxInjectAnnotProxyFieldValueFactoryTest()
	{
		super(new JavaxInjectInjectable());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5686
	 * @throws Exception
	 */
	@Test
	public void required() throws Exception
	{
		Field field = obj.getClass().getDeclaredField("nonExisting");
		try
		{
			factory.getFieldValue(field, obj);
			fail("Fields annotated with @Inject are required!");
		}
		catch (IllegalStateException isx)
		{
			// expected
			assertTrue(true);
		}
	}
}
