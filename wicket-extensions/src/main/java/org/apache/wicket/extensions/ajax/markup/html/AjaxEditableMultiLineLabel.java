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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * An inplace editor much like {@link AjaxEditableLabel}, but now with support for multi line
 * content and a {@link TextArea text area} as its editor.
 * 
 * @author eelcohillenius
 * 
 * @param <T>
 *            Model object type
 */
public class AjaxEditableMultiLineLabel<T> extends AjaxEditableLabel<T>
{
	private static final long serialVersionUID = 1L;

	/** text area's number of rows. */
	private int rows = 10;

	/** text area's number of columns. */
	private int cols = 40;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 */
	public AjaxEditableMultiLineLabel(final String id)
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
	public AjaxEditableMultiLineLabel(final String id, final IModel<T> model)
	{
		super(id, model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected MultiLineLabel newLabel(final MarkupContainer parent, final String componentId,
		final IModel<T> model)
	{
		MultiLineLabel label = new MultiLineLabel(componentId, model)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void onComponentTagBody(final MarkupStream markupStream,
				final ComponentTag openTag)
			{
				Object modelObject = getDefaultModelObject();
				if ((modelObject == null) || "".equals(modelObject))
				{
					replaceComponentTagBody(markupStream, openTag, defaultNullLabel());
				}
				else
				{
					super.onComponentTagBody(markupStream, openTag);
				}
			}
		};
		label.setOutputMarkupId(true);
		label.add(new LabelAjaxBehavior(getLabelAjaxEvent()));
		return label;
	}

	/**
	 * By default this returns "onclick" uses can overwrite this on which event the label behavior
	 * should be triggered
	 * 
	 * @return The event name
	 */
	@Override
	protected String getLabelAjaxEvent()
	{
		return "onclick";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected FormComponent<T> newEditor(final MarkupContainer parent, final String componentId,
		final IModel<T> model)
	{
		TextArea<T> editor = new TextArea<T>(componentId, model)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void onModelChanged()
			{
				AjaxEditableMultiLineLabel.this.onModelChanged();
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void onModelChanging()
			{
				AjaxEditableMultiLineLabel.this.onModelChanging();
			}
		};
		editor.add(new AttributeModifier("rows", new AbstractReadOnlyModel<Integer>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Integer getObject()
			{
				return rows;
			}
		}));
		editor.add(new AttributeModifier("cols", new AbstractReadOnlyModel<Integer>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Integer getObject()
			{
				return cols;
			}
		}));
		editor.setOutputMarkupId(true);
		editor.setVisible(false);
		editor.add(new EditorAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void onComponentTag(final ComponentTag tag)
			{
				super.onComponentTag(tag);
				final String saveCall = "{wicketAjaxPost('" + getCallbackUrl() + "&save=true', " +
					"wicketSerialize(this)); return true;}";

				final String cancelCall = "{wicketAjaxGet('" + getCallbackUrl() +
					"&save=false');this.onblur='';return false;}";

				final String keypress = "var kc=wicketKeyCode(event); if (kc==27) " + cancelCall +
					"; ";

				tag.put("onblur", saveCall);
				tag.put("onkeydown", keypress);
			}
		});
		return editor;
	}

	/**
	 * Gets text area's number of columns.
	 * 
	 * @return text area's number of columns
	 */
	public final int getCols()
	{
		return cols;
	}

	/**
	 * Sets text area's number of columns.
	 * 
	 * @param cols
	 *            text area's number of columns
	 */
	public final void setCols(final int cols)
	{
		this.cols = cols;
	}

	/**
	 * Gets text area's number of rows.
	 * 
	 * @return text area's number of rows
	 */
	public final int getRows()
	{
		return rows;
	}

	/**
	 * Sets text area's number of rows.
	 * 
	 * @param rows
	 *            text area's number of rows
	 */
	public final void setRows(final int rows)
	{
		this.rows = rows;
	}

	/**
	 * Override this to display a different value when the model object is null. Default is
	 * <code>...</code>
	 * 
	 * @return The string which should be displayed when the model object is null.
	 */
	@Override
	protected String defaultNullLabel()
	{
		return "...";
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
}
