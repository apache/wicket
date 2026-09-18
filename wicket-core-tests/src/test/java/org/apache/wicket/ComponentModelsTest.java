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
package org.apache.wicket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IWrapModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests a component with models beside its default model.
 */
class ComponentModelsTest extends WicketTestCase
{
	@Test
	void constructorAddsAdditionalModels()
	{
		IModel<String> first = Model.of("first");
		IModel<String> second = Model.of("second");
		IModel<String> third = Model.of("third");

		TestComponent component = new TestComponent("c", first, second, null, third);

		assertSame(first, component.getDefaultModel());
		assertEquals(Arrays.asList(first, second, third), component.getModels());
	}

	@Test
	void defaultModelIsStillInheritedWithAdditionalModels()
	{
		WebMarkupContainer parent = new WebMarkupContainer("parent",
			new CompoundPropertyModel<>(new Bean()));
		IModel<String> additional = Model.of("additional");
		TestComponent child = new TestComponent("name", null, additional);
		parent.add(child);

		assertEquals(Arrays.asList(additional), child.getModels());
		assertEquals("bean", child.getDefaultModelObject());
	}

	@Test
	void allModelsAreDetachedAtTheEndOfTheRequest()
	{
		CountingModel defaultModel = new CountingModel();
		CountingModel additional = new CountingModel();
		TestComponent component = new TestComponent("c", defaultModel);
		component.additional = component.addAdditionalModel(additional);

		tester.startComponentInPage(component);

		assertEquals(1, defaultModel.loads);
		assertEquals(1, additional.loads);
		assertFalse(defaultModel.isAttached());
		assertFalse(additional.isAttached());
	}

	@Test
	void addingTwiceAddsOnce()
	{
		IModel<String> model = Model.of("model");
		TestComponent component = new TestComponent("c", null);

		assertSame(model, component.addAdditionalModel(model));
		assertSame(model, component.addAdditionalModel(model));

		assertEquals(Arrays.asList(model), component.getModels());
	}

	@Test
	void addingNullAddsNothing()
	{
		TestComponent component = new TestComponent("c", null);

		assertNull(component.addAdditionalModel(null));

		assertTrue(component.getModels().isEmpty());
	}

	@Test
	void registeringReturnsTheGivenModelWithItsType()
	{
		TestComponent component = new TestComponent("c", null);

		CountingModel model = component.addAdditionalModel(new CountingModel());

		assertEquals(Arrays.asList(model), component.getModels());
		assertSame(model, component.removeAdditionalModel(model));
		assertTrue(component.getModels().isEmpty());
	}

	@Test
	void registeredModelIsNotWrapped()
	{
		AssignedModel assigned = new AssignedModel();
		TestComponent component = new TestComponent("c", null);

		assertSame(assigned, component.addAdditionalModel(assigned));

		assertNull(assigned.component);
		assertEquals(Arrays.asList(assigned), component.getModels());
	}

	@Test
	void wrappedModelIsRegisteredAsTheWrapper()
	{
		AssignedModel assigned = new AssignedModel();
		TestComponent component = new TestComponent("c", null);

		IModel<String> wrapped = component.addAdditionalModel(component.wrap(assigned));

		assertSame(component, assigned.component);
		assertSame(assigned, component.addAdditionalModel(assigned));
		assertEquals(Arrays.asList(wrapped), component.getModels());

		assertSame(assigned, component.removeAdditionalModel(assigned));
		assertTrue(component.getModels().isEmpty());
	}

	@Test
	void replaceDetachesAndRemovesThePreviousModel()
	{
		CountingModel previous = new CountingModel();
		IModel<String> other = Model.of("other");
		TestComponent component = new TestComponent("c", null);
		component.addAdditionalModel(previous);
		component.addAdditionalModel(other);
		previous.getObject();

		IModel<String> next = Model.of("next");
		assertSame(next, component.replaceAdditionalModel(previous, next));

		assertFalse(previous.isAttached());
		assertEquals(Arrays.asList(other, next), component.getModels());
	}

	@Test
	void replaceWithTheSameModelKeepsIt()
	{
		TestComponent component = new TestComponent("c", null);
		CountingModel model = component.addAdditionalModel(new CountingModel());
		model.getObject();

		assertSame(model, component.replaceAdditionalModel(model, model));

		assertTrue(model.isAttached());
		assertEquals(Arrays.asList(model), component.getModels());
	}

	@Test
	void replaceWithNullRemoves()
	{
		IModel<String> model = Model.of("model");
		TestComponent component = new TestComponent("c", null);
		component.addAdditionalModel(model);

		assertNull(component.replaceAdditionalModel(model, null));

		assertTrue(component.getModels().isEmpty());
	}

	@Test
	void removingAModelThatWasNotAddedHasNoEffect()
	{
		IModel<String> model = Model.of("model");
		TestComponent component = new TestComponent("c", null);
		component.addAdditionalModel(model);

		component.removeAdditionalModel(Model.of("other"));
		component.removeAdditionalModel(null);

		assertEquals(Arrays.asList(model), component.getModels());
	}

	private static class TestComponent extends WebComponent
	{
		private static final long serialVersionUID = 1L;

		private IModel<?> additional;

		TestComponent(String id, IModel<?> model, IModel<?>... additionalModels)
		{
			super(id, model, additionalModels);
		}

		@Override
		protected void onComponentTag(ComponentTag tag)
		{
			super.onComponentTag(tag);

			getDefaultModelObject();
			additional.getObject();
		}
	}

	private static class AssignedModel implements IComponentAssignedModel<String>
	{
		private static final long serialVersionUID = 1L;

		private Component component;

		@Override
		public String getObject()
		{
			return null;
		}

		@Override
		public IWrapModel<String> wrapOnAssignment(Component component)
		{
			this.component = component;
			return new IWrapModel<>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public IModel<?> getWrappedModel()
				{
					return AssignedModel.this;
				}

				@Override
				public String getObject()
				{
					return null;
				}
			};
		}
	}

	private static class CountingModel extends LoadableDetachableModel<String>
	{
		private static final long serialVersionUID = 1L;

		private int loads;

		@Override
		protected String load()
		{
			loads++;
			return "loaded";
		}
	}

	private static class Bean implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused")
		private final String name = "bean";
	}
}
