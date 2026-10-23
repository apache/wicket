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
package org.apache.wicket.examples.builder.spike;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.danekja.java.misc.serializable.SerializableRunnable;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;

/**
 * SPIKE - throwaway. The builders behind {@link CoreUi}.
 * <p>
 * Every anonymous component subclass is created in a static method. An anonymous class created
 * in an instance method keeps a reference to the builder, which would then be serialized with the
 * page.
 */
public final class Builders
{
	private Builders()
	{
	}

	public static final class Simple<C extends org.apache.wicket.Component>
		extends ComponentBuilder<C, Simple<C>>
	{
		private final Function<String, C> factory;

		public Simple(String id, Function<String, C> factory)
		{
			super(id);
			this.factory = factory;
		}

		@Override
		protected C create(String id)
		{
			return factory.apply(id);
		}
	}

	public abstract static class AbstractContainerBuilder<C extends MarkupContainer, T, B extends AbstractContainerBuilder<C, T, B>>
		extends ComponentBuilder<C, B>
	{
		private Consumer<Scope<T>> body = scope -> {
		};

		protected AbstractContainerBuilder(String id)
		{
			super(id);
		}

		public B body(Consumer<Scope<T>> body)
		{
			this.body = body;
			return self();
		}

		@Override
		@SuppressWarnings("unchecked")
		public C build()
		{
			C container = super.build();
			body.accept(new Scope<>(container, (IModel<T>)container.getDefaultModel()));
			return container;
		}
	}

	public static final class ContainerBuilder<T>
		extends AbstractContainerBuilder<WebMarkupContainer, T, ContainerBuilder<T>>
	{
		private final IModel<T> model;

		ContainerBuilder(String id, IModel<T> model)
		{
			super(id);
			this.model = model;
		}

		@Override
		protected WebMarkupContainer create(String id)
		{
			return new WebMarkupContainer(id, model);
		}
	}

	public static final class FormBuilder<T>
		extends AbstractContainerBuilder<Form<T>, T, FormBuilder<T>>
	{
		private final IModel<T> model;

		private SerializableConsumer<T> onSubmit = object -> {
		};

		FormBuilder(String id, IModel<T> model)
		{
			super(id);
			this.model = model;
		}

		public FormBuilder<T> onSubmit(SerializableConsumer<T> onSubmit)
		{
			this.onSubmit = onSubmit;
			return this;
		}

		@Override
		protected Form<T> create(String id)
		{
			return newForm(id, model, onSubmit);
		}

		private static <T> Form<T> newForm(String id, IModel<T> model,
			SerializableConsumer<T> onSubmit)
		{
			return new Form<>(id, model)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit()
				{
					onSubmit.accept(getModelObject());
				}
			};
		}
	}

	public static final class ListBuilder<T> extends ComponentBuilder<ListView<T>, ListBuilder<T>>
	{
		private final IModel<? extends List<T>> items;

		private SerializableConsumer<Scope<T>> each = row -> {
		};

		ListBuilder(String id, IModel<? extends List<T>> items)
		{
			super(id);
			this.items = items;
		}

		public ListBuilder<T> each(SerializableConsumer<Scope<T>> each)
		{
			this.each = each;
			return this;
		}

		@Override
		protected ListView<T> create(String id)
		{
			return newListView(id, items, each);
		}

		private static <T> ListView<T> newListView(String id, IModel<? extends List<T>> items,
			SerializableConsumer<Scope<T>> each)
		{
			return new ListView<>(id, items)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<T> item)
				{
					each.accept(new Scope<>(item, item.getModel()));
				}
			};
		}
	}

	public static final class LinkBuilder extends ComponentBuilder<Link<Void>, LinkBuilder>
	{
		private SerializableRunnable onClick = () -> {
		};

		LinkBuilder(String id)
		{
			super(id);
		}

		public LinkBuilder onClick(SerializableRunnable onClick)
		{
			this.onClick = onClick;
			return this;
		}

		@Override
		protected Link<Void> create(String id)
		{
			return newLink(id, onClick);
		}

		private static Link<Void> newLink(String id, SerializableRunnable onClick)
		{
			return new Link<>(id)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					onClick.run();
				}
			};
		}
	}

	public static final class AjaxLinkBuilder extends ComponentBuilder<AjaxLink<Void>, AjaxLinkBuilder>
	{
		private SerializableConsumer<AjaxRequestTarget> onClick = target -> {
		};

		AjaxLinkBuilder(String id)
		{
			super(id);
		}

		public AjaxLinkBuilder onClick(SerializableConsumer<AjaxRequestTarget> onClick)
		{
			this.onClick = onClick;
			return this;
		}

		@Override
		protected AjaxLink<Void> create(String id)
		{
			return newAjaxLink(id, onClick);
		}

		private static AjaxLink<Void> newAjaxLink(String id,
			SerializableConsumer<AjaxRequestTarget> onClick)
		{
			return new AjaxLink<>(id)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					onClick.accept(target);
				}
			};
		}
	}

	public static final class AjaxButtonBuilder extends ComponentBuilder<AjaxButton, AjaxButtonBuilder>
	{
		private SerializableConsumer<AjaxRequestTarget> onSubmit = target -> {
		};

		private SerializableConsumer<AjaxRequestTarget> onError = target -> {
		};

		AjaxButtonBuilder(String id)
		{
			super(id);
		}

		public AjaxButtonBuilder onSubmit(SerializableConsumer<AjaxRequestTarget> onSubmit)
		{
			this.onSubmit = onSubmit;
			return this;
		}

		public AjaxButtonBuilder onError(SerializableConsumer<AjaxRequestTarget> onError)
		{
			this.onError = onError;
			return this;
		}

		@Override
		protected AjaxButton create(String id)
		{
			return newAjaxButton(id, onSubmit, onError);
		}

		private static AjaxButton newAjaxButton(String id,
			SerializableConsumer<AjaxRequestTarget> onSubmit,
			SerializableConsumer<AjaxRequestTarget> onError)
		{
			return new AjaxButton(id)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target)
				{
					onSubmit.accept(target);
				}

				@Override
				protected void onError(AjaxRequestTarget target)
				{
					onError.accept(target);
				}
			};
		}
	}

	public static final class AjaxCheckBoxBuilder
		extends ComponentBuilder<AjaxCheckBox, AjaxCheckBoxBuilder>
	{
		private final IModel<Boolean> model;

		private SerializableConsumer<AjaxRequestTarget> onUpdate = target -> {
		};

		AjaxCheckBoxBuilder(String id, IModel<Boolean> model)
		{
			super(id);
			this.model = model;
		}

		public AjaxCheckBoxBuilder onUpdate(SerializableConsumer<AjaxRequestTarget> onUpdate)
		{
			this.onUpdate = onUpdate;
			return this;
		}

		@Override
		protected AjaxCheckBox create(String id)
		{
			return newAjaxCheckBox(id, model, onUpdate);
		}

		private static AjaxCheckBox newAjaxCheckBox(String id, IModel<Boolean> model,
			SerializableConsumer<AjaxRequestTarget> onUpdate)
		{
			return new AjaxCheckBox(id, model)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target)
				{
					onUpdate.accept(target);
				}
			};
		}
	}

	static <T> IConverter<T> converter(SerializableFunction<String, T> parse,
		SerializableFunction<T, String> format)
	{
		return new IConverter<>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public T convertToObject(String value, Locale locale) throws ConversionException
			{
				try
				{
					return parse.apply(value);
				}
				catch (RuntimeException e)
				{
					throw new ConversionException(e).setSourceValue(value)
						.setConverter(this)
						.setLocale(locale);
				}
			}

			@Override
			public String convertToString(T value, Locale locale)
			{
				return format.apply(value);
			}
		};
	}
}
