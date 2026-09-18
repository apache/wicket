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
package org.apache.wicket.benchmarks;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * The component shapes compared by {@link AdditionalModelsFootprint} and
 * {@link AdditionalModelsBenchmark}: the same component holding the same extra models, once with a
 * hand written {@code onDetach()} and once letting {@code Component} detach them.
 * <p>
 * Every variant carries a default model and the given number of extra models, all of them the same
 * kind of model, so a difference between two variants is the bookkeeping alone. The components are
 * static classes with no reference to anything outside, because they are serialized.
 */
public final class AdditionalModelsShapes
{
	private AdditionalModelsShapes()
	{
	}

	/** The models are all of this kind, in every variant. */
	static IModel<String> model(String value)
	{
		return Model.of(value);
	}

	/** How a component holds the models beside its default model. */
	public enum Variant
	{
		/** A field per model, detached by a hand written onDetach. The way before Wicket 11. */
		FIELDS
		{
			@Override
			Component newComponent(String id, int extraModels)
			{
				return new FieldModels(id, extraModels);
			}
		},
		/** A field per model, each registered, no onDetach. */
		REGISTERED
		{
			@Override
			Component newComponent(String id, int extraModels)
			{
				return new RegisteredModels(id, extraModels);
			}
		},
		/** Registered and never referenced again, so the component needs no fields at all. */
		REGISTERED_NO_FIELDS
		{
			@Override
			Component newComponent(String id, int extraModels)
			{
				return new RegisteredModelsNoFields(id, extraModels);
			}
		};

		abstract Component newComponent(String id, int extraModels);
	}

	/**
	 * Keeps its extra models in fields and detaches them itself. Three fields, because a field is
	 * what a component needs to reach a model from onDetach, and unused ones cost what they cost.
	 */
	static class FieldModels extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		private final IModel<String> first;

		private final IModel<String> second;

		private final IModel<String> third;

		FieldModels(String id, int extraModels)
		{
			super(id, model(id));

			first = extraModels > 0 ? model(id + "-1") : null;
			second = extraModels > 1 ? model(id + "-2") : null;
			third = extraModels > 2 ? model(id + "-3") : null;
		}

		@Override
		protected void onDetach()
		{
			if (first != null)
			{
				first.detach();
			}
			if (second != null)
			{
				second.detach();
			}
			if (third != null)
			{
				third.detach();
			}
			super.onDetach();
		}
	}

	/** Keeps its extra models in fields and lets Component detach them. */
	static class RegisteredModels extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		private final IModel<String> first;

		private final IModel<String> second;

		private final IModel<String> third;

		RegisteredModels(String id, int extraModels)
		{
			super(id, model(id));

			first = extraModels > 0 ? addAdditionalModel(model(id + "-1")) : null;
			second = extraModels > 1 ? addAdditionalModel(model(id + "-2")) : null;
			third = extraModels > 2 ? addAdditionalModel(model(id + "-3")) : null;
		}
	}

	/** Registers its extra models and keeps no reference to them. */
	static class RegisteredModelsNoFields extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		RegisteredModelsNoFields(String id, int extraModels)
		{
			super(id, model(id));

			for (int i = 1; i <= extraModels; i++)
			{
				addAdditionalModel(model(id + "-" + i));
			}
		}
	}
}
