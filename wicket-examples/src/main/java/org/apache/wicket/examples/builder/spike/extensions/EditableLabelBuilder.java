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
package org.apache.wicket.examples.builder.spike.extensions;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.examples.builder.spike.ComponentBuilder;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableChoiceLabel;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableMultiLineLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.io.IClusterable;
import org.danekja.java.util.function.serializable.SerializableConsumer;

/**
 * SPIKE - throwaway. Builds the three in-place editors of wicket-extensions.
 * <p>
 * Each editor needs an anonymous subclass to hook into submit, error and conversion, so the hooks
 * travel in a serializable {@link Hooks} object that the subclass consults.
 *
 * @param <T>
 *            the type of the edited value
 */
public final class EditableLabelBuilder<T>
	extends ComponentBuilder<AjaxEditableLabel<T>, EditableLabelBuilder<T>>
{
	private enum Kind
	{
		SINGLE_LINE, MULTI_LINE, CHOICE
	}

	private static final class Hooks<T> implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		private SerializableConsumer<AjaxRequestTarget> onSubmit = target -> {
		};

		private SerializableConsumer<AjaxRequestTarget> onError = target -> {
		};

		private IConverter<T> converter;
	}

	private final Kind kind;

	private final IModel<T> model;

	private final List<? extends T> choices;

	private final Hooks<T> hooks = new Hooks<>();

	private EditableLabelBuilder(String id, Kind kind, IModel<T> model, List<? extends T> choices)
	{
		super(id);
		this.kind = kind;
		this.model = model;
		this.choices = choices;
	}

	static <T> EditableLabelBuilder<T> singleLine(String id, IModel<T> model)
	{
		return new EditableLabelBuilder<>(id, Kind.SINGLE_LINE, model, null);
	}

	static <T> EditableLabelBuilder<T> multiLine(String id, IModel<T> model)
	{
		return new EditableLabelBuilder<>(id, Kind.MULTI_LINE, model, null);
	}

	static <T> EditableLabelBuilder<T> choice(String id, IModel<T> model,
		List<? extends T> choices)
	{
		return new EditableLabelBuilder<>(id, Kind.CHOICE, model, choices);
	}

	public EditableLabelBuilder<T> onSubmit(SerializableConsumer<AjaxRequestTarget> onSubmit)
	{
		hooks.onSubmit = onSubmit;
		return this;
	}

	public EditableLabelBuilder<T> onError(SerializableConsumer<AjaxRequestTarget> onError)
	{
		hooks.onError = onError;
		return this;
	}

	public EditableLabelBuilder<T> converter(IConverter<T> converter)
	{
		hooks.converter = converter;
		return this;
	}

	@Override
	protected AjaxEditableLabel<T> create(String id)
	{
		return switch (kind)
		{
			case SINGLE_LINE -> newSingleLine(id, model, hooks);
			case MULTI_LINE -> newMultiLine(id, model, hooks);
			case CHOICE -> newChoice(id, model, choices, hooks);
		};
	}

	private static <T> AjaxEditableLabel<T> newSingleLine(String id, IModel<T> model,
		Hooks<T> hooks)
	{
		return new AjaxEditableLabel<>(id, model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				super.onSubmit(target);
				hooks.onSubmit.accept(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				super.onError(target);
				hooks.onError.accept(target);
			}

			@Override
			protected IConverter<?> createConverter(Class<?> type)
			{
				return hooks.converter != null ? hooks.converter : super.createConverter(type);
			}
		};
	}

	private static <T> AjaxEditableLabel<T> newMultiLine(String id, IModel<T> model,
		Hooks<T> hooks)
	{
		return new AjaxEditableMultiLineLabel<>(id, model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				super.onSubmit(target);
				hooks.onSubmit.accept(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				super.onError(target);
				hooks.onError.accept(target);
			}

			@Override
			protected IConverter<?> createConverter(Class<?> type)
			{
				return hooks.converter != null ? hooks.converter : super.createConverter(type);
			}
		};
	}

	private static <T> AjaxEditableLabel<T> newChoice(String id, IModel<T> model,
		List<? extends T> choices, Hooks<T> hooks)
	{
		return new AjaxEditableChoiceLabel<>(id, model, choices)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				super.onSubmit(target);
				hooks.onSubmit.accept(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				super.onError(target);
				hooks.onError.accept(target);
			}

			@Override
			protected IConverter<?> createConverter(Class<?> type)
			{
				return hooks.converter != null ? hooks.converter : super.createConverter(type);
			}
		};
	}
}
