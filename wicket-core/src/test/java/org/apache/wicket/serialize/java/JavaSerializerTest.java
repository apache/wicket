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
package org.apache.wicket.serialize.java;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.core.util.objects.checker.CheckingObjectOutputStream;
import org.apache.wicket.core.util.objects.checker.IObjectChecker;
import org.apache.wicket.core.util.objects.checker.NotDetachedModelChecker;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.io.IOUtils;
import org.junit.Test;

/**
 *
 */
public class JavaSerializerTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4812
	 *
	 * Tests that the serialization fails when a checking ObjectOutputStream is
	 * used with NotDetachedModelChecker and there is a non-detached LoadableDetachableModel
	 * in the object tree.
	 */
	@Test
	public void notDetachedModel()
	{
		JavaSerializer serializer = new JavaSerializer("JavaSerializerTest")
		{
			@Override
			protected ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException
			{
				IObjectChecker checker = new NotDetachedModelChecker();
				return new CheckingObjectOutputStream(out, checker);
			}
		};

		IModel<String> model = new NotDetachedModel();
		model.getObject();
		WebComponent component = new WebComponent("id", model);
		byte[] serialized = serializer.serialize(component);
		assertNull("The produced byte[] must be null if there was an error", serialized);
	}

	/**
	 * A Model used for #notDetachedModel() test
	 */
	private static class NotDetachedModel extends LoadableDetachableModel<String>
	{
		@Override
		protected String load()
		{
			return "loaded";
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4812
	 * 
	 * Tests that serialization fails when using the default ObjectOutputStream in
	 * JavaSerializer and some object in the tree is not Serializable
	 */
	@Test
	public void notSerializable()
	{
		JavaSerializer serializer = new JavaSerializer("JavaSerializerTest");
		WebComponent component = new NotSerializableComponent("id");
		byte[] serialized = serializer.serialize(component);
		assertNull("The produced byte[] must be null if there was an error", serialized);
	}

	private static class NotSerializableComponent extends WebComponent
	{
		private final NotSerializableObject member = new NotSerializableObject();

		public NotSerializableComponent(final String id)
		{
			super(id);
		}
	}

	private static class NotSerializableObject {}

	@Test
	public void normal()
	{
		JavaSerializer serializer = new JavaSerializer("JavaSerializerTest-normal") {
			@Override
			protected ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException
			{
				return new CheckingObjectOutputStream(out);
			}
		};
		byte[] bytes = serializer.serialize("Something to serialize");
		assertEquals(57, bytes.length);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5667
	 */
	@Test
	public void preserveTheOriginalException()
	{
		JavaSerializer serializer = new JavaSerializer("JavaSerializerTest-aa")
		{
			// Override serialize to re-throw the exception instead of just logging it
			// The exception is used later to make the assertions
			@Override
			public byte[] serialize(Object object)
			{
				try
				{
					final ByteArrayOutputStream out = new ByteArrayOutputStream();
					ObjectOutputStream oos = null;
					try
					{
						oos = newObjectOutputStream(out);
						oos.writeObject("applicationKey");
						oos.writeObject(object);
					}
					finally
					{
						try
						{
							IOUtils.close(oos);
						}
						finally
						{
							out.close();
						}
					}
					return out.toByteArray();
				}
				catch (Exception x)
				{
					throw new RuntimeException(x);
				}
			}
		};
		try
		{
			serializer.serialize(new ObjectThatBlowsOnSerialization());
			fail("The serialization should have failed!");
		}
		catch (Exception x)
		{
			Throwable cause0 = x.getCause();
			assertThat(cause0, is(instanceOf(WicketRuntimeException.class)));
			WicketRuntimeException wrx = (WicketRuntimeException) cause0;

			Throwable cause1 = wrx.getCause();
			assertThat(cause1, is(instanceOf(IllegalStateException.class)));
			assertThat(cause1.getMessage(), is(equalTo("Cannot serialize me twice!")));

			Throwable cause2 = cause1.getCause();
			assertThat(cause2, is(instanceOf(NotSerializableException.class)));
		}
	}

	private static class ObjectThatBlowsOnSerialization implements Serializable
	{
		private int counter = 0;

		private void writeObject(ObjectOutputStream oos) throws IOException
		{
			counter++;
			if (counter == 1)
			{
				throw new NotSerializableException();
			}
			throw new IllegalStateException("Cannot serialize me twice!");
		}
	}
}
