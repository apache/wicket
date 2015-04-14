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
package org.apache.wicket.markup.html.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testing updating model collections through
 * {@link FormComponent#updateCollectionModel(FormComponent)}.
 * 
 * @author svenmeier
 */
public class CollectionFormComponentTest extends WicketTestCase
{
	@Test
	public void getSetNullCollection()
	{
		final AtomicBoolean setCalled = new AtomicBoolean();

		Object object = new Object()
		{
			private Collection<String> internal = null;

			public Collection<String> getStrings()
			{
				return internal;
			}

			public void setStrings(Collection<String> strings)
			{
				this.internal = strings;

				setCalled.set(true);
			}
		};

		Choice choice = new Choice(object);
		choice.setConvertedInput(Arrays.asList("A", "B"));
		FormComponent.updateCollectionModel(choice);

		assertEquals(true, setCalled.get());
		assertEquals("[A, B]", choice.getDefaultModelObjectAsString());
	}

	@Test(expected = WicketRuntimeException.class)
	public void getNullCollectionFails()
	{
		Object object = new Object()
		{
			private Collection<String> internal = null;

			public Collection<String> getStrings()
			{
				return internal;
			}
		};

		Choice choice = new Choice(object);
		choice.setConvertedInput(Arrays.asList("A", "B"));
		FormComponent.updateCollectionModel(choice);
	}

	@Test
	public void getSetModifiableCollection()
	{
		final AtomicBoolean setCalled = new AtomicBoolean();

		Object object = new Object()
		{
			private Collection<String> internal = new ArrayList<String>();

			public Collection<String> getStrings()
			{
				return internal;
			}

			public void setStrings(Collection<String> strings)
			{
				this.internal = strings;

				setCalled.set(true);
			}
		};

		Choice choice = new Choice(object);
		choice.setConvertedInput(Arrays.asList("A", "B"));
		FormComponent.updateCollectionModel(choice);

		assertEquals(true, setCalled.get());
		assertEquals("[A, B]", choice.getDefaultModelObjectAsString());
	}

	@Test
	public void getModifiableCollection()
	{
		Object object = new Object()
		{
			private Collection<String> internal = new ArrayList<String>();

			public Collection<String> getStrings()
			{
				return internal;
			}
		};

		Choice choice = new Choice(object);
		choice.setConvertedInput(Arrays.asList("A", "B"));
		FormComponent.updateCollectionModel(choice);

		assertEquals("[A, B]", choice.getDefaultModelObjectAsString());
	}

	/**
	 * WICKET-5518
	 */
	@Test
	public void getSetUnmodifiableCollection()
	{
		final AtomicBoolean setCalled = new AtomicBoolean();

		Object object = new Object()
		{
			private Collection<String> internal = new ArrayList<String>();

			public Collection<String> getStrings()
			{
				return Collections.unmodifiableCollection(internal);
			}

			public void setStrings(Collection<String> strings)
			{
				this.internal = strings;

				setCalled.set(true);
			}
		};

		Choice choice = new Choice(object);
		choice.setConvertedInput(Arrays.asList("A", "B"));
		FormComponent.updateCollectionModel(choice);

		assertEquals(true, setCalled.get());
		assertEquals("[A, B]", choice.getDefaultModelObjectAsString());
	}

	@Test(expected = WicketRuntimeException.class)
	public void getUnmodifiableFails()
	{
		Object object = new Object()
		{
			private Collection<String> internal = new ArrayList<String>();

			public Collection<String> getStrings()
			{
				return Collections.unmodifiableCollection(internal);
			}
		};

		Choice choice = new Choice(object);
		choice.setConvertedInput(Arrays.asList("A", "B"));
		FormComponent.updateCollectionModel(choice);
	}

	@Test
	public void getUnmodifiableInCaseOfNoConvertedInput()
	{
		LoadableDetachableModel<Collection<String>> model = new LoadableDetachableModel<Collection<String>>()
		{

			@Override
			protected Collection<String> load()
			{
				return Collections.unmodifiableList(Arrays.asList("1", "2"));
			}

		};
		FormComponent<Collection<String>> formComponent = new FormComponent<Collection<String>>(
			"formComponent", model)
		{

		};
		formComponent.setConvertedInput(null);
		FormComponent.updateCollectionModel(formComponent);
		Assert.assertTrue(formComponent.getModelObject().isEmpty());
	}

	@Test
	public void getModelCollectionIsNullInCaseOfNoConvertedInput()
	{
		LoadableDetachableModel<Collection<String>> model = new LoadableDetachableModel<Collection<String>>()
		{

			@Override
			protected Collection<String> load()
			{
				return null;
			}

		};
		FormComponent<Collection<String>> formComponent = new FormComponent<Collection<String>>(
				"formComponent", model)
		{

		};
		formComponent.setConvertedInput(null);
		FormComponent.updateCollectionModel(formComponent);
		Assert.assertTrue(formComponent.getModelObject().isEmpty());
	}

	private class Choice extends FormComponent<Collection<String>>
	{

		public Choice(Object object)
		{
			super("choice", new PropertyModel(object, "strings"));
		}
	}

}
