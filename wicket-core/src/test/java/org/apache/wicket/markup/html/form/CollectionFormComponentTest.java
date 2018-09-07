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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Testing updating model collections through
 * {@link FormComponent#updateCollectionModel(FormComponent)}.
 * 
 * @author svenmeier
 */
class CollectionFormComponentTest extends WicketTestCase
{
	@Test
	void getSetNullList()
	{
		final AtomicBoolean setCalled = new AtomicBoolean();

		Object object = new Object()
		{
			private List<String> internal = null;

			public List<String> getStrings()
			{
				return internal;
			}

			public void setStrings(List<String> strings)
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
	void getSetNullSet()
	{
		final AtomicBoolean setCalled = new AtomicBoolean();

		Object object = new Object()
		{
			private Set<String> internal = null;

			public Set<String> getStrings()
			{
				return internal;
			}

			public void setStrings(Set<String> strings)
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
	void getNullCollectionFails()
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

		assertThrows(WicketRuntimeException.class, () -> {
			FormComponent.updateCollectionModel(choice);
		});
	}

	@Test
	void getSetModifiableCollection()
	{
		final AtomicBoolean setCalled = new AtomicBoolean();

		Object object = new Object()
		{
			private Collection<String> internal = new ArrayList<>();
			
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
	void getModifiableCollection()
	{
		Object object = new Object()
		{
			private Collection<String> internal = new ArrayList<>();
			
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
	void getSetUnmodifiableList()
	{
		final AtomicBoolean setCalled = new AtomicBoolean();

		Object object = new Object()
		{
			private List<String> internal = new ArrayList<>();
			
			public List<String> getStrings()
			{
				return Collections.unmodifiableList(internal);
			}

			public void setStrings(List<String> strings)
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
	void getSetUnmodifiableSet()
	{
		final AtomicBoolean setCalled = new AtomicBoolean();

		Object object = new Object()
		{
			private Set<String> internal = new HashSet<>();
			
			public Set<String> getStrings()
			{
				return Collections.unmodifiableSet(internal);
			}

			public void setStrings(Set<String> strings)
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
	void getUnmodifiableFails()
	{
		Object object = new Object()
		{
			private Collection<String> internal = new ArrayList<>();
			
			public Collection<String> getStrings()
			{
				return Collections.unmodifiableCollection(internal);
			}
		};

		Choice choice = new Choice(object);
		choice.setConvertedInput(Arrays.asList("A", "B"));

		assertThrows(WicketRuntimeException.class, () -> {
			FormComponent.updateCollectionModel(choice);
		});
	}

	@Test
	void getUnmodifiableInCaseOfNoConvertedInput()
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
		assertTrue(formComponent.getModelObject().isEmpty());
	}

	@Test
	void getModelCollectionIsNullInCaseOfNoConvertedInput()
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
		assertTrue(formComponent.getModelObject().isEmpty());
	}

	private class Choice extends FormComponent<Collection<String>>
	{

		Choice(Object object)
		{
			super("choice", new PropertyModel(object, "strings"));
		}
	}

}
