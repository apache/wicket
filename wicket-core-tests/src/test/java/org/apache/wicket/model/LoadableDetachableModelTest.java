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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests the states of a LoadableDetachableModel
 */
@SuppressWarnings("javadoc")
class LoadableDetachableModelTest extends WicketTestCase
{
	/**
	 * Checks whether the LDM can escape recursive calls.
	 */
	@Test
	void recursiveGetObjectDoesntCauseInfiteLoop()
	{
		class RecursiveLoad extends LoadableDetachableModel<Integer>
		{
			private static final long serialVersionUID = 1L;

			private int count = 0;

			@Override
			protected Integer load()
			{
				count++;
				getObject();
				return count;
			}
		}

		RecursiveLoad ldm = new RecursiveLoad();

		assertEquals(false, ldm.isAttached());
		assertThat(ldm.getObject()).isEqualTo(1);
		assertEquals(true, ldm.isAttached());
	}

	@Test
	void onAttachCalled()
	{
		class AttachingLoadableModel extends LoadableDetachableModel<Integer>
		{
			private static final long serialVersionUID = 1L;

			private boolean attachCalled = false;

			@Override
			protected Integer load()
			{
				return null;
			}

			@Override
			protected void onAttach()
			{
				attachCalled = true;
			}
		}

		AttachingLoadableModel m = new AttachingLoadableModel();
		m.getObject();

		assertEquals(true, m.isAttached());
		assertEquals(true, m.attachCalled);
	}

	/**
	 * Checks whether the LDM can escape recursive calls.
	 */
	@Test
	void exceptionDuringLoadKeepsLDMDetached()
	{
		class ExceptionalLoad extends LoadableDetachableModel<Integer>
		{
			private static final long serialVersionUID = 1L;

			private boolean detachCalled = false;

			@Override
			protected Integer load()
			{
				throw new RuntimeException();
			}

			@Override
			protected void onDetach()
			{
				detachCalled = true;
			}
		}

		ExceptionalLoad ldm = new ExceptionalLoad();

		assertEquals(false, ldm.isAttached());
		try
		{
			assertThat(ldm.getObject()).isEqualTo(1);
			fail("shouldn't get here");
		}
		catch (RuntimeException e)
		{
		}
		ldm.detach();
		assertEquals(false, ldm.isAttached());
		assertEquals(true, ldm.detachCalled);
	}

	private static class SerializedLoad extends LoadableDetachableModel<Integer>
	{
		private static final long serialVersionUID = 1L;

		private int count = 0;

		@Override
		protected Integer load()
		{
			return ++count;
		}
	}

	/**
	 * Tests serialization/deserialization of LDM retaining the correct state.
	 * 
	 * @throws Exception
	 */
	@Test
	void serializationDeserializationRetainsInternalState() throws Exception
	{
		SerializedLoad ldm = new SerializedLoad();
		assertThat(ldm.getObject()).isEqualTo(1);
		ldm.detach();

		byte[] serialized = serialize(ldm);

		LoadableDetachableModel<Integer> deserialized = deserialize(serialized);

		assertEquals(false, deserialized.isAttached());
		assertThat(deserialized.getObject()).isEqualTo(2);
		assertEquals(true, deserialized.isAttached());
		deserialized.detach();
		assertEquals(false, deserialized.isAttached());
	}

	/** Serialization helper */
	@SuppressWarnings("unchecked")
	private <T> LoadableDetachableModel<T> deserialize(byte[] serialized)
		throws IOException, ClassNotFoundException
	{
		LoadableDetachableModel<T> deserialized = null;

		try (ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
			ObjectInputStream ois = new ObjectInputStream(bais);)
		{
			deserialized = (LoadableDetachableModel<T>)ois.readObject();
		}
		return deserialized;
	}

	/** Deserialization helper */
	private byte[] serialize(Serializable ldm) throws IOException
	{
		byte[] stream = { };
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);)
		{
			oos.writeObject(ldm);
			stream = baos.toByteArray();
		}
		return stream;
	}
}
