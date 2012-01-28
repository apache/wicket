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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.Strings;


/**
 * Component that represents a <code>&lt;select&gt;</code> box. Elements are provided by one or more
 * <code>SelectChoice</code> or <code>SelectOptions</code> components in the hierarchy below the
 * <code>Select</code> component.
 * 
 * Advantages to the standard choice components is that the user has a lot more control over the
 * markup between the &lt;select&gt; tag and its children &lt;option&gt; tags: allowing for such
 * things as &lt;optgroup&gt; tags.
 * 
 * <p>
 * Example HTML:
 * 
 * <pre>
 *    &lt;select wicket:id=&quot;select&quot; multiple=&quot;multiple&quot;&gt;
 *        &lt;wicket:container wicket:id=&quot;options&quot;&gt;
 *            &lt;option wicket:id=&quot;option&quot;&gt;Option Label&lt;/option&gt;
 *        &lt;/wicket:container&gt;
 *    &lt;/select&gt;
 * </pre>
 * 
 * Related Java Code:
 * 
 * <pre>
 * Select select = new Select(&quot;select&quot;, selectionModel);
 * add(select);
 * SelectOptions options = new SelectOptions(&quot;options&quot;, elements, renderer);
 * select.add(options);
 * </pre>
 * 
 * Note that you don't need to add component(s) for the &lt;option&gt; tag - they are created by
 * SelectOptions
 * <p>
 * 
 * @see SelectOption
 * @see SelectOptions
 * 
 * @author Igor Vaynberg
 */
public class Select<T> extends FormComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor that will create a default model collection
	 * 
	 * @param id
	 *            component id
	 */
	public Select(String id)
	{
		super(id);
	}

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public Select(String id, IModel<T> model)
	{
		super(id, model);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void convertInput()
	{
		boolean supportsMultiple = getDefaultModelObject() instanceof Collection;

		/*
		 * the input contains an array of values of the selected option components unless nothing
		 * was selected in which case the input contains null
		 */
		String[] values = getInputAsArray();

		if (values == null || values.length == 0)
		{
			setConvertedInput(null);
			return;
		}

		if (!supportsMultiple && values.length > 1)
		{
			throw new WicketRuntimeException(
				"The model of Select component [" +
					getPath() +
					"] is not of type java.util.Collection, but more then one SelectOption component has been selected. Either remove the multiple attribute from the select tag or make the model of the Select component a collection");
		}

		List<Object> converted = new ArrayList<Object>(values.length);

		/*
		 * if the input is null we do not need to do anything since the model collection has already
		 * been cleared
		 */
		for (int i = 0; i < values.length; i++)
		{
			final String value = values[i];
			if (!Strings.isEmpty(value))
			{
				SelectOption<T> option = (SelectOption<T>)visitChildren(SelectOption.class,
					new Component.IVisitor<SelectOption<T>>()
					{
						public Object component(SelectOption<T> option)
						{
							if (String.valueOf(option.getValue()).equals(value))
							{
								return option;
							}
							return CONTINUE_TRAVERSAL;
						}
					});

				if (option == null)
				{
					throw new WicketRuntimeException(
						"submitted http post value [" +
							Strings.join(",", values) +
							"] for SelectOption component [" +
							getPath() +
							"] contains an illegal value [" +
							value +
							"] which does not point to a SelectOption component. Due to this the Select component cannot resolve the selected SelectOption component pointed to by the illegal value. A possible reason is that component hierarchy changed between rendering and form submission.");
				}
				converted.add(option.getDefaultModelObject());
			}
		}

		if (converted.isEmpty())
		{
			setConvertedInput(null);
		}
		else if (!supportsMultiple)
		{
			setConvertedInput((T)converted.get(0));
		}
		else
		{
			setConvertedInput((T)converted);
		}
	}


	/**
	 * @see FormComponent#updateModel()
	 */
	@Override
	public void updateModel()
	{
		T object = getModelObject();
		boolean supportsMultiple = object instanceof Collection;

		Object converted = getConvertedInput();
		/*
		 * update the model
		 */
		if (supportsMultiple)
		{
			Collection modelCollection = (Collection)object;
			modelChanging();
			modelCollection.clear();
			if (converted != null)
			{
				modelCollection.addAll((Collection)converted);
			}
			modelChanged();
			// force notify of model update via setObject()
			setDefaultModelObject(modelCollection);
		}
		else
		{
			setDefaultModelObject(converted);
		}
	}

	/**
	 * Checks if the specified option is selected
	 * 
	 * @param option
	 * @return {@code true} if the option is selected, {@code false} otherwise
	 */
	boolean isSelected(SelectOption<?> option)
	{
		// if the raw input is specified use that, otherwise use model
		if (hasRawInput())
		{
			final String raw = getRawInput();
			if (!Strings.isEmpty(raw))
			{
				String[] values = raw.split(VALUE_SEPARATOR);
				for (int i = 0; i < values.length; i++)
				{
					String value = values[i];
					if (value.equals(option.getValue()))
					{
						return true;
					}
				}
			}
			return false;
		}

		return compareModels(getDefaultModelObject(), option.getDefaultModelObject());
	}

	private boolean compareModels(Object selected, Object value)
	{
		if (selected != null && selected instanceof Collection)
		{
			if (value instanceof Collection)
			{
				return ((Collection)selected).containsAll((Collection)value);
			}
			else
			{
				return ((Collection)selected).contains(value);
			}
		}
		else
		{
			return Objects.equal(selected, value);
		}
	}

}
