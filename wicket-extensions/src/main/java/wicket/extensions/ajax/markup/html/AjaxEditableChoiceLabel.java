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
package wicket.extensions.ajax.markup.html;

import java.util.List;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.ajax.ClientEvent;
import wicket.markup.ComponentTag;
import wicket.markup.html.basic.MultiLineLabel;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.markup.html.form.TextArea;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * And inplace editor much like {@link AjaxEditableLabel}, but now with support
 * for multi line content and a {@link TextArea text area} as its editor.
 * 
 * @param <T>
 *            The type
 * 
 * @author Eelco Hillenius
 */
public class AjaxEditableChoiceLabel<T> extends AjaxEditableLabel<T>
{
	private static final long serialVersionUID = 1L;

	/** The list of objects. */
	private IModel<List<T>> choices;

	/** The renderer used to generate display/id values for the objects. */
	private IChoiceRenderer<T> renderer;

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 */
	public AjaxEditableChoiceLabel(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 */
	public AjaxEditableChoiceLabel(MarkupContainer parent, String id, IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AjaxEditableChoiceLabel(MarkupContainer parent, String id, List<T> choices)
	{
		this(parent, id, null, choices);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AjaxEditableChoiceLabel(MarkupContainer parent, String id, IModel<T> model,
			IModel<List<T>> choices)
	{
		super(parent, id, model);
		this.choices = choices;
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @param renderer
	 *            The rendering engine
	 */
	public AjaxEditableChoiceLabel(MarkupContainer parent, String id, IModel<T> model,
			IModel<List<T>> choices, IChoiceRenderer<T> renderer)
	{
		super(parent, id, model);
		this.choices = choices;
		this.renderer = renderer;
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public AjaxEditableChoiceLabel(MarkupContainer parent, String id, IModel<T> model,
			List<T> choices)
	{
		this(parent, id, model, new Model<List<T>>(choices));
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @param renderer
	 *            The rendering engine
	 */
	public AjaxEditableChoiceLabel(MarkupContainer parent, String id, IModel<T> model,
			List<T> choices, IChoiceRenderer<T> renderer)
	{
		this(parent, id, model, new Model<List<T>>(choices), renderer);
	}

	/**
	 * @see wicket.extensions.ajax.markup.html.AjaxEditableLabel#newLabel(wicket.MarkupContainer,
	 *      java.lang.String, wicket.model.IModel)
	 */
	@Override
	protected Component newLabel(MarkupContainer parent, String componentId, IModel<T> model)
	{
		MultiLineLabel label = new MultiLineLabel(parent, componentId, model);
		label.setOutputMarkupId(true);
		label.add(new LabelAjaxBehavior(ClientEvent.CLICK));
		return label;
	}

	/**
	 * @see wicket.extensions.ajax.markup.html.AjaxEditableLabel#newEditor(wicket.MarkupContainer,
	 *      java.lang.String, wicket.model.IModel)
	 */
	@Override
	protected FormComponent<T> newEditor(MarkupContainer parent, String componentId, IModel<T> model)
	{
		DropDownChoice<T> editor = new DropDownChoice<T>(parent, componentId, model, choices,
				renderer);
		editor.setOutputMarkupId(true);
		editor.setVisible(false);
		editor.add(new EditorAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				final String saveCall = "{wicketAjaxGet('" + getCallbackUrl()
						+ "&save=true&'+this.name+'='+wicketEncode(this.value)); return true;}";

				final String cancelCall = "{wicketAjaxGet('" + getCallbackUrl()
						+ "&save=false'); return false;}";

				tag.put(ClientEvent.CHANGE.getEvent(), saveCall);
			}
		});
		return editor;
	}
}
