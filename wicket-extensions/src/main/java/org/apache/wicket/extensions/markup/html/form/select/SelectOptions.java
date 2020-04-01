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
package org.apache.wicket.extensions.markup.html.form.select;

import java.util.Collection;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Component that makes it easy to produce a list of SelectOption components.
 * <p>
 * Has to be attached to a &lt;option&gt; markup tag.
 * <p>
 * Note: The following pre Wicket 9 markup is deprecated and results in a log warning. Its support
 * will be removed in Wicket 10:
 *
 * <pre>
 * <code>
 * &lt;wicket:container wicket:id=&quot;selectOptions&quot;&gt;&lt;option wicket:id=&quot;option&quot;&gt;&lt;/option&gt;&lt;/wicket:container&gt;
 * </code>
 * </pre>
 * 
 * @param <T>
 *            type of elements contained in the model's collection
 * @author Igor Vaynberg (ivaynberg)
 */
public class SelectOptions<T> extends RepeatingView
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(SelectOptions.class);

	private boolean recreateChoices = false;

	private final IOptionRenderer<T> renderer;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 * @param renderer
	 */
	public SelectOptions(final String id, final IModel<? extends Collection<? extends T>> model,
		final IOptionRenderer<T> renderer)
	{
		super(id, model);
		this.renderer = renderer;
		setRenderBodyOnly(true);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param elements
	 * @param renderer
	 */
	public SelectOptions(final String id, final Collection<? extends T> elements,
		final IOptionRenderer<T> renderer)
	{
		this(id, new CollectionModel<>(elements), renderer);
	}

	/**
	 * Controls whether {@link SelectOption}s are recreated on each render.
	 * <p>
	 * Note: When recreating on each render, {@link #newOption(String, IModel)} should return
	 * {@link SelectOption}s with stable values, i.e. {@link SelectOption#getValue()} should return
	 * a value based on its model object instead of the default auto index. Otherwise the current
	 * selection will be lost on form errors.
	 * 
	 * @param refresh
	 * @return this for chaining
	 * 
	 * @see SelectOption#getValue()
	 */
	public SelectOptions<T> setRecreateChoices(final boolean refresh)
	{
		recreateChoices = refresh;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected final void onPopulate()
	{
		if ((size() == 0) || recreateChoices)
		{
			// populate this repeating view with SelectOption components
			removeAll();

			Collection<? extends T> modelObject = (Collection<? extends T>)getDefaultModelObject();
			if (modelObject != null)
			{
				// TODO remove in Wicket 10
				boolean option = "option".equalsIgnoreCase(getMarkupTag().getName());
				if (option == false)
				{
					log.warn("Since version 9.0.0 you should use an option tag");
				}

				for (T value : modelObject)
				{
					// we add our actual SelectOption component to the row
					String text = renderer.getDisplayValue(value);
					IModel<T> model = renderer.getModel(value);

					if (option)
					{
						add(newOption(newChildId(), text, model));
					}
					else
					{
						// pre Wicket 9 a container is used to represent a row in repeater
						WebMarkupContainer row = new WebMarkupContainer(newChildId());
						row.setRenderBodyOnly(true);
						add(row);

						// we add our actual SelectOption component to the row
						row.add(newOption(text, model));
					}
				}
			}
		}
	}

	/**
	 * @deprecated override {@link #newOption(String, String, IModel)} instead.
	 */
	protected SelectOption<T> newOption(final String text, final IModel<T> model)
	{
		return newOption("option",  text, model);
	}

	/**
	 * Factory method for creating a new <code>SelectOption</code>. Override to add your own
	 * extensions, such as Ajax behaviors.
	 * 
	 * @param id
	 *            component id
	 * @param text
	 * @param model
	 * @return a {@link SelectOption}
	 */
	protected SelectOption<T> newOption(final String id, final String text, final IModel<T> model)
	{
		SimpleSelectOption<T> option = new SimpleSelectOption<>(id, model, text);
		option.setEscapeModelStrings(this.getEscapeModelStrings());
		return option;
	}

	/**
	 * 
	 * @param <V>
	 */
	private static class SimpleSelectOption<V> extends SelectOption<V>
	{
		private static final long serialVersionUID = 1L;

		private final String text;

		/**
		 * @param id
		 * @param model
		 * @param text
		 */
		public SimpleSelectOption(final String id, final IModel<V> model, final String text)
		{
			super(id, model);
			this.text = text;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
		{
			CharSequence escaped = text;
			if (getEscapeModelStrings())
			{
				escaped = Strings.escapeMarkup(text);
			}

			replaceComponentTagBody(markupStream, openTag, escaped);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void onComponentTag(ComponentTag tag)
		{
			super.onComponentTag(tag);

			// always transform the tag to <label></label> so even markup defined as <label/>
			// render
			tag.setType(TagType.OPEN);
		}
	}

	@Override
	protected void onDetach()
	{
		renderer.detach();

		super.onDetach();
	}
}
