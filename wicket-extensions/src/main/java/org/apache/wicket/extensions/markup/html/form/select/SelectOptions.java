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
import org.apache.wicket.model.util.WildcardCollectionModel;


/**
 * Component that makes it easy to produce a list of SelectOption components
 * 
 * Example markup:
 * 
 * <pre>
 * <code>
 * &lt;wicket:container wicket:id=&quot;selectOptions&quot;&gt;&lt;option wicket:id=&quot;option&quot;&gt;&lt;/option&gt;&lt;/wicket:container&gt;
 * </code>
 * </pre>
 * 
 * @param <T>
 * @author Igor Vaynberg (ivaynberg)
 */
public class SelectOptions<T> extends RepeatingView
{
	private static final long serialVersionUID = 1L;

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
		this(id, new WildcardCollectionModel<>(elements), renderer);
	}

	/**
	 * Controls whether {@link SelectOption}s are recreated on every request
	 * <p>
	 * Note: When refreshing on each render, {@link #newOption(String, IModel)} should return
	 * {@link SelectOption}s with stable values, otherwise the current selection will be lost on
	 * form errors.
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
				for (T value : modelObject)
				{
					// we need a container to represent a row in repeater
					WebMarkupContainer row = new WebMarkupContainer(newChildId());
					row.setRenderBodyOnly(true);
					add(row);

					// we add our actual SelectOption component to the row
					String text = renderer.getDisplayValue(value);
					IModel<T> model = renderer.getModel(value);
					row.add(newOption(text, model));
				}
			}
		}
	}

	/**
	 * Factory method for creating a new <code>SelectOption</code>. Override to add your own
	 * extensions, such as Ajax behaviors.
	 * 
	 * @param text
	 * @param model
	 * @return a {@link SelectOption}
	 */
	protected SelectOption<T> newOption(final String text, final IModel<? extends T> model)
	{
		return new SimpleSelectOption<>("option", model, text);
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
		public SimpleSelectOption(final String id, final IModel<? extends V> model, final String text)
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
			replaceComponentTagBody(markupStream, openTag, text);
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
}
