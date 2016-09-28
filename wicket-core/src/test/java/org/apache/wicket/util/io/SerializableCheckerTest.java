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
package org.apache.wicket.util.io;

import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.core.util.objects.checker.CheckingObjectOutputStream;
import org.apache.wicket.core.util.objects.checker.ObjectSerializationChecker;
import org.apache.wicket.core.util.objects.checker.AbstractObjectChecker;
import org.apache.wicket.core.util.objects.checker.CheckingObjectOutputStream;
import org.apache.wicket.core.util.objects.checker.IObjectChecker;
import org.apache.wicket.util.value.ValueMap;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class SerializableCheckerTest extends Assert
{

	/**
	 * Test {@link ValueMap} serializability.
	 * 
	 * @throws IOException
	 */
	@Test
	public void valueMap() throws IOException
	{
		CheckingObjectOutputStream checker = new CheckingObjectOutputStream(new ByteArrayOutputStream(),
				new ObjectSerializationChecker(new NotSerializableException()));
		checker.writeObject(new ValueMap());
	}

	/**
	 * Asserts that {@link org.apache.wicket.core.util.objects.checker.CheckingObjectOutputStream}
	 * will check an instance just once, despite it occurs more than once in the object tree
	 *
	 * https://issues.apache.org/jira/browse/WICKET-5642
	 *
	 * @throws IOException
	 */
	@Test
	public void checkObjectsByIdentity() throws IOException
	{
		CountingChecker countingChecker = new CountingChecker();
		CheckingObjectOutputStream outputStream = new CheckingObjectOutputStream(new ByteArrayOutputStream(), countingChecker);
		final IdentityTestType type = new IdentityTestType();
		type.member = new SerializableTypeWithMember(type);
		outputStream.writeObject(type);

		assertThat(countingChecker.getCount(), is(2));
	}

	private static class CountingChecker extends AbstractObjectChecker
	{
		private int count = 0;

		@Override
		public Result check(Object object)
		{
			count++;
			return super.check(object);
		}

		private int getCount()
		{
			return count;
		}
	}

	private static class SerializableTypeWithMember extends SerializableType
	{
		private final IdentityTestType member;

		private SerializableTypeWithMember(IdentityTestType member)
		{
			this.member = member;
		}
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void nonSerializableTypeDetection() throws IOException
	{
		CheckingObjectOutputStream checker = new CheckingObjectOutputStream(new ByteArrayOutputStream(),
			new ObjectSerializationChecker(new NotSerializableException()));
		String exceptionMessage = null;
		try
		{
			checker.writeObject(new TestType2());
		}
		catch (CheckingObjectOutputStream.ObjectCheckException e)
		{
			exceptionMessage = e.getMessage();
		}
		assertTrue(exceptionMessage.contains(NonSerializableType.class.getName()));
	}

	private static class IdentityTestType implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private SerializableType member;

		@Override
		public boolean equals(Object obj)
		{
			return false;
		}
	}

	private static class TestType2 implements Serializable
	{
		private static final long serialVersionUID = 1L;
		ProblematicType problematicType = new ProblematicType();
		SerializableType serializableType = new SerializableType();
		NonSerializableType nonSerializable = new NonSerializableType();
	}

	private static class NonSerializableType
	{
	}

	private static class SerializableType implements Serializable
	{
		private static final long serialVersionUID = 1L;
	}
	private static class TestException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

	}
	private static class ProblematicType implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean equals(Object obj)
		{
			throw new TestException();
		}
	}
}
