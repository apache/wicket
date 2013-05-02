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
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
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

	private final IMocksControl[] ctrl = new IMocksControl[4];

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
			ctrl[i] = EasyMock.createControl();
			fact[i] = ctrl[i].createMock(IFieldValueFactory.class);
		}
	}

	protected void prepare(final int cnt)
	{
		for (int i = 0; i < cnt; i++)
		{
			EasyMock.expect(fact[i].getFieldValue(field, this)).andReturn(null);
			ctrl[i].replay();
		}
	}

	protected void verify(final int cnt)
	{
		for (int i = 0; i < cnt; i++)
		{
			ctrl[i].verify();
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
		verify(2);

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
		verify(4);

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
		verify(2);

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
		EasyMock.expect(fact[2].getFieldValue(field, this)).andReturn(new Object());
		ctrl[2].replay();
		ctrl[3].replay();
		List<IFieldValueFactory> list = Arrays.asList(fact[0], fact[1], fact[2], fact[3]);
		CompoundFieldValueFactory f = new CompoundFieldValueFactory(list);

		f.getFieldValue(field, this);

		verify(4);
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
		verify(3);

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
