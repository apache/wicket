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
package wicket.injection;

import java.lang.reflect.Field;

import junit.framework.TestCase;

/**
 * Test {@link NoopFieldValueFactory}
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class NoopFieldValueFactoryTest extends TestCase
{
	private Integer testField;

	private Field field;

	protected void setUp() throws Exception
	{
		Field field = NoopFieldValueFactoryTest.class.getDeclaredField("testField");
	}

	/**
	 * make sure null is returned
	 */
	public void test()
	{
		NoopFieldValueFactory fact = new NoopFieldValueFactory();
		assertNull(fact.getFieldValue(field, this));
	}

}
