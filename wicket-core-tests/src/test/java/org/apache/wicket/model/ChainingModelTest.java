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
package org.apache.wicket.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link ChainingModel}.
 * 
 * @author svenmeier
 */
class ChainingModelTest
{
	@Test
	void testNonModel() {
		ChainingModel<Integer> model = new ChainingModel<>(1);
		
		assertEquals(Integer.valueOf(1), model.getObject());
		model.setObject(2);
		assertEquals(Integer.valueOf(2), model.getObject());
	}
	
	@Test
	void testNonSerializable() {
		new ChainingModel<>(Thread.currentThread());
	}
	
	@Test
	void testDetachable() {
		class TestDetachable implements IDetachable
		{
			boolean detached = false;
			
			@Override
			public void detach()
			{
				detached = true;
			}
		};
		TestDetachable test = new TestDetachable();
		
		ChainingModel<TestDetachable> model = new ChainingModel<>(test);
		assertSame(test,  model.getObject());

		test = new TestDetachable();
		model.setObject(test);
		assertSame(test,  model.getObject());

		model.detach();
		assertTrue(test.detached);
	}

	@Test
	void testModel() {
		class TestModel implements IModel<Integer>
		{
			boolean detached = false;
			int value = 1;
			
			@Override
			public Integer getObject()
			{
				return value;
			}
			
			@Override
			public void setObject(Integer object)
			{
				this.value = object;
			}
			
			@Override
			public void detach()
			{
				detached = true;
			}
		};
		TestModel test = new TestModel();
		
		ChainingModel<Integer> model = new ChainingModel<>(test);
		
		assertEquals(Integer.valueOf(1), model.getObject());

		model.setObject(2);
		assertEquals(2, test.value);
		
		model.detach();
		assertTrue(test.detached);
	}
}