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
package org.apache.wicket.core.util.objects.checker;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.wicket.MockPageWithOneComponent;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for {@link NotDetachedModelChecker}.
 * <p>
 * Tests that the serialization fails when a checking ObjectOutputStream is
 * used with NotDetachedModelChecker and there is a non-detached LoadableDetachableModel
 * in the object tree.
 * </p>
 */
public class NotDetachedModelCheckerTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4812
	 * https://issues.apache.org/jira/browse/WICKET-6334
	 */
	@Test
	public void whenSerializingPage_thenItsComponentsShouldBeChecked() {
		JavaSerializer serializer = new JavaSerializer("JavaSerializerTest")
		{
			@Override
			protected ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException
			{
				IObjectChecker checker = new NotDetachedModelChecker();
				return new CheckingObjectOutputStream(out, checker);
			}
		};

		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(new ComponentWithAttachedModel(MockPageWithOneComponent.COMPONENT_ID));

		final byte[] serialized = serializer.serialize(page);
		assertNull("The produced byte[] must be null if there was an error", serialized);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4812
	 * https://issues.apache.org/jira/browse/WICKET-6334
	 */
	@Test
	public void whenSerializingNonPageComponent_thenItsSubComponentsShouldNotBeChecked() {
		JavaSerializer serializer = new JavaSerializer("JavaSerializerTest")
		{
			@Override
			protected ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException
			{
				IObjectChecker checker = new NotDetachedModelChecker();
				return new CheckingObjectOutputStream(out, checker);
			}
		};

		final ComponentWithAttachedModel component = new ComponentWithAttachedModel("id");

		final byte[] serialized = serializer.serialize(component);
		assertThat(serialized, is(notNullValue()));
	}

	private static class ComponentWithAttachedModel extends WebComponent
	{
		private final IModel<String> member = new LoadableDetachableModel<String>()
		{
			@Override
			protected String load()
			{
				return "modelObject";
			}
		};

		public ComponentWithAttachedModel(final String id)
		{
			super(id);

			// attach the model object
			member.getObject();
		}
	}
}
