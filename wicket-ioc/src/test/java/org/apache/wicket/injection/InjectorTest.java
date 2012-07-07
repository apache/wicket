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
package org.apache.wicket.injection;

import java.lang.reflect.Field;

import org.apache.wicket.injection.util.MockDependency;
import org.apache.wicket.injection.util.TestObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link Injector}
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class InjectorTest extends Assert
{
	private static MockDependency dependency = new MockDependency("inject");

	private static IFieldValueFactory factory = new IFieldValueFactory()
	{
		@Override
		public Object getFieldValue(final Field field, final Object fieldOwner)
		{
			return dependency;
		}

		@Override
		public boolean supportsField(final Field field)
		{
			return true;
		}

	};

	private static class TestInjector extends Injector
	{

		@Override
		public void inject(final Object object)
		{
			inject(object, factory);
		}

	}

	/**
	 * Test injection
	 */
	@Test
	public void testInjection()
	{
		TestObject testObject = new TestObject();

		new TestInjector().inject(testObject, factory);

		assertEquals(testObject.getDependency1().getMessage(), "inject");
		assertEquals(testObject.getDependency2().getMessage(), "dont-inject");
		assertEquals(testObject.getDependency3().getMessage(), "dont-inject");
		assertEquals(testObject.getDependency4().getMessage(), "inject");
	}


}
