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
package wicket.extensions.markup.html.form.select;

import java.util.Collection;

import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.repeater.RepeatingView;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Component that makes it easy to produce a list of SelectOption components
 * 
 * @param <T>
 *            Type of the collection objects
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class SelectOptions<T> extends RepeatingView<Collection<T>>
{
	private static final long serialVersionUID = 1L;
	
	private boolean recreateChoices = false;
	
	private IOptionRenderer<T> renderer;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 * @param model
	 * @param renderer
	 */
	public SelectOptions(MarkupContainer parent, final String id, IModel<Collection<T>> model,
			IOptionRenderer<T> renderer)
	{
		super(parent, id, model);
		
		this.renderer = renderer;
		setRenderBodyOnly(true);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 * @param elements
	 * @param renderer
	 */
	public SelectOptions(MarkupContainer parent, final String id, Collection<T> elements,
			IOptionRenderer<T> renderer)
	{
		this(parent, id, new Model<Collection<T>>(elements), renderer);
	}

	/**
	 * Controls whether or not SelectChoice objects are recreated every request
	 * 
	 * @param refresh
	 * @return this for chaining
	 */
	public SelectOptions setRecreateChoices(boolean refresh)
	{
		this.recreateChoices = refresh;
		return this;
	}

	@Override
	protected void onAttach()
	{
		super.onAttach();
		
		if (size() == 0 || recreateChoices)
		{
			// populate this repeating view with SelectOption components
			removeAll();

			Collection<T> modelObject = getModelObject();

			if (modelObject != null)
			{
				if (!(modelObject instanceof Collection))
				{
					throw new WicketRuntimeException("Model object " + modelObject
							+ " not a collection");
				}

				// iterator over model objects for SelectOption components
				for (T value : modelObject)
				{
					// we need a container to represent a row in repeater
					WebMarkupContainer row = new WebMarkupContainer(this, newChildId());
					row.setRenderBodyOnly(true);

					// we add our actual SelectOption component to the row
					String text = renderer.getDisplayValue(value);
					IModel<T> model = renderer.getModel(value);
					new SimpleSelectOption<T>(row, "option", model, text);
				}
			}
		}
	}

	private static class SimpleSelectOption<T> extends SelectOption<T>
	{
		private static final long serialVersionUID = 1L;

		private String text;

		/**
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 * @param model
		 * @param text
		 */
		public SimpleSelectOption(MarkupContainer parent, final String id, IModel<T> model, String text)
		{
			super(parent, id, model);
			this.text = text;
		}

		@Override
		protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
		{
			replaceComponentTagBody(markupStream, openTag, text);
		}
	}
}
