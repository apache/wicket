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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link CompoundFieldValueFactory}
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class CompoundFieldValueFactoryTest extends Assert
{
	private Integer testField;

	private Field field;

	private final IFieldValueFactory[] fact = new IFieldValueFactory[4];

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		CompoundFieldValueFactoryTest.class.getDeclaredField("testField");

		for (int i = 0; i < 4; i++)
		{
			fact[i] = mock(IFieldValueFactory.class);
		}
	}

	protected void prepare(final int cnt)
	{
		for (int i = 0; i < cnt; i++)
		{
			when(fact[i].getFieldValue(field, this)).thenReturn(null);
		}
	}

	protected void verifyCalled(int... indices)
	{
		for (int i : indices)
		{
			verify(fact[i], times(1)).getFieldValue(field, this);
		}
	}

	private void verifyNotCalled(int... indices)
	{
		for (int i : indices)
		{
			verify(fact[i], never()).getFieldValue(field, this);
		}
	}

	/**
	 * Test array constructor
	 */
	@Test
	public void testArrayConstructor()
	{
		prepare(2);
		CompoundFieldValueFactory f = new CompoundFieldValueFactory(new IFieldValueFactory[] {
				fact[0], fact[1] });
		f.getFieldValue(field, this);
		verifyCalled(0, 1);

		try
		{
			f = new CompoundFieldValueFactory((IFieldValueFactory[])null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}
	}

	/**
	 * Test list constructor
	 */
	@Test
	public void testListConstructor()
	{
		prepare(4);
		List<IFieldValueFactory> list = Arrays.asList(fact[0], fact[1], fact[2], fact[3]);
		CompoundFieldValueFactory f = new CompoundFieldValueFactory(list);
		f.getFieldValue(field, this);
		verifyCalled(0, 1, 2, 3);

		try
		{
			f = new CompoundFieldValueFactory((List<IFieldValueFactory>)null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}

	}

	/**
	 * Test list constructor
	 */
	@Test
	public void testABConstructor()
	{
		prepare(2);
		CompoundFieldValueFactory f = new CompoundFieldValueFactory(fact[0], fact[1]);
		f.getFieldValue(field, this);
		verifyCalled(0, 1);

		try
		{
			f = new CompoundFieldValueFactory(fact[0], null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}
		try
		{
			f = new CompoundFieldValueFactory(null, fact[1]);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}

	}

	/**
	 * Test list constructor
	 */
	@Test
	public void testBreakOnNonNullReturn()
	{
		prepare(2);
		when(fact[2].getFieldValue(field, this)).thenReturn(new Object());
		List<IFieldValueFactory> list = Arrays.asList(fact[0], fact[1], fact[2], fact[3]);
		CompoundFieldValueFactory f = new CompoundFieldValueFactory(list);

		f.getFieldValue(field, this);

		verifyCalled(0, 1, 2);
		verifyNotCalled(3);
	}

	/**
	 * Test addFactory()
	 */
	@Test
	public void testAdd()
	{
		prepare(3);
		CompoundFieldValueFactory f = new CompoundFieldValueFactory(new IFieldValueFactory[] {
				fact[0], fact[1] });
		f.addFactory(fact[2]);
		f.getFieldValue(field, this);
		verifyCalled(0, 1, 2);

		try
		{
			f.addFactory(null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}
	}

}
