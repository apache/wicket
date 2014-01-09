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
package org.apache.wicket.extensions.ajax.markup.html;

import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.Strings;


/**
 * An inplace editor much like {@link AjaxEditableLabel}, but instead of a {@link TextField} a
 * {@link DropDownChoice} is displayed.
 * 
 * @param <T>
 * @author Eelco Hillenius
 */
public class AjaxEditableChoiceLabel<T> extends AjaxEditableLabel<T>
{
	private static final long serialVersionUID = 1L;

	/** The list of objects. */
	private IModel<? extends List<? extends T>> choices;

	/** The renderer used to generate display/id values for the objects. */
	private ChoiceRenderer<T> renderer;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 */
	public AjaxEditableChoiceLabel(final String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 */
	public AjaxEditableChoiceLabel(final String id, final IModel<T> model)
	{
		super(id, model);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AjaxEditableChoiceLabel(final String id, final List<? extends T> choices)
	{
		this(id, null, choices);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AjaxEditableChoiceLabel(final String id, final IModel<T> model,
		final IModel<? extends List<? extends T>> choices)
	{
		super(id, model);
		this.choices = choices;
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @param renderer
	 *            The rendering engine
	 */
	public AjaxEditableChoiceLabel(final String id, final IModel<T> model,
		final IModel<? extends List<? extends T>> choices, final ChoiceRenderer<T> renderer)
	{
		super(id, model);
		this.choices = choices;
		this.renderer = renderer;
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AjaxEditableChoiceLabel(final String id, final IModel<T> model,
		final List<? extends T> choices)
	{
		this(id, model, Model.ofList(choices));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @param renderer
	 *            The rendering engine
	 */
	public AjaxEditableChoiceLabel(final String id, final IModel<T> model,
		final List<? extends T> choices, final ChoiceRenderer<T> renderer)
	{
		this(id, model, Model.ofList(choices), renderer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected FormComponent<T> newEditor(final MarkupContainer parent, final String componentId,
		final IModel<T> model)
	{
		IModel<List<? extends T>> choiceModel = new AbstractReadOnlyModel<List<? extends T>>()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public List<? extends T> getObject()
			{
				return choices.getObject();
			}

		};

		DropDownChoice<T> editor = new DropDownChoice<T>(componentId, model, choiceModel, renderer)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onModelChanged()
			{
				AjaxEditableChoiceLabel.this.onModelChanged();
			}

			@Override
			protected void onModelChanging()
			{
				AjaxEditableChoiceLabel.this.onModelChanging();
			}

		};

		editor.setOutputMarkupId(true);
		editor.setVisible(false);
		editor.add(new EditorAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				attributes.setEventNames("change", "blur", "keyup");

				CharSequence dynamicExtraParameters = "var result = [], "
						+ "kc=Wicket.Event.keyCode(attrs.event),"
						+ "evtType=attrs.event.type;"
						+ "if (evtType === 'blur' || (evtType === 'keyup' && kc===27)) {"
						+ "  result.push( { name: 'save', value: false } );"
						+ "}"
						+ "else {"
						+ "  result = Wicket.Form.serializeElement(attrs.c);"
						+ "  result.push( { name: 'save', value: true } );"
						+ "}"
						+ "return result;";
				attributes.getDynamicExtraParameters().add(dynamicExtraParameters);

				CharSequence precondition = "var kc=Wicket.Event.keyCode(attrs.event),"
						+ "evtType=attrs.event.type,"
						+ "ret=false;"
						+ "if(evtType==='blur' || evtType==='change' || (evtType==='keyup' && kc===27)) ret = true;"
						+ "return ret;";
				AjaxCallListener ajaxCallListener = new AjaxCallListener();
				ajaxCallListener.onPrecondition(precondition);
				attributes.getAjaxCallListeners().add(ajaxCallListener);
			}
		});
		return editor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected WebComponent newLabel(final MarkupContainer parent, final String componentId,
		final IModel<T> model)
	{
		Label label = new Label(componentId, model)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * {@inheritDoc}
			 */
			@Override
			public <C> IConverter<C> getConverter(final Class<C> type)
			{
				IConverter<C> c = AjaxEditableChoiceLabel.this.getConverter(type);
				return c != null ? c : super.getConverter(type);
			}

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("unchecked")
			@Override
			public void onComponentTagBody(final MarkupStream markupStream,
				final ComponentTag openTag)
			{
				String displayValue = getDefaultModelObjectAsString();
				if (renderer != null)
				{
					Object displayObject = renderer.getDisplayValue((T)getDefaultModelObject());
					Class<?> objectClass = (displayObject == null ? null : displayObject.getClass());

					if ((objectClass != null) && (objectClass != String.class))
					{
						@SuppressWarnings("rawtypes")
						final IConverter converter = getConverter(objectClass);
						displayValue = converter.convertToString(displayObject, getLocale());
					}
					else if (displayObject != null)
					{
						displayValue = displayObject.toString();
					}
				}

				if (Strings.isEmpty(displayValue))
				{
					replaceComponentTagBody(markupStream, openTag, defaultNullLabel());
				}
				else
				{
					replaceComponentTagBody(markupStream, openTag, displayValue);
				}
			}
		};
		label.setOutputMarkupId(true);
		label.add(new LabelAjaxBehavior(getLabelAjaxEvent()));
		return label;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onModelChanged()
	{
		super.onModelChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onModelChanging()
	{
		super.onModelChanging();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDetach()
	{
		if (choices != null)
		{
			choices.detach();
		}
		super.onDetach();
	}
}
