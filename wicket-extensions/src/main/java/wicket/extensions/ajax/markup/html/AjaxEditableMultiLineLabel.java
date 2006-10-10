/*
 * $Id$ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.ajax.markup.html;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.MarkupContainer;
import wicket.ajax.ClientEvent;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.basic.MultiLineLabel;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.TextArea;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;

/**
 * And inplace editor much like {@link AjaxEditableLabel}, but now with support
 * for multi line content and a {@link TextArea text area} as its editor.
 * 
 * @param <T>
 *            The type
 * 
 * @author eelcohillenius
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
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 */
	public AjaxEditableMultiLineLabel(MarkupContainer parent, String id)
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
	public AjaxEditableMultiLineLabel(MarkupContainer parent, String id, IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * @see wicket.extensions.ajax.markup.html.AjaxEditableLabel#newLabel(wicket.MarkupContainer,
	 *      java.lang.String, wicket.model.IModel)
	 */
	@Override
	protected Component newLabel(MarkupContainer parent, String componentId, IModel<T> model)
	{
		MultiLineLabel label = new MultiLineLabel(this, componentId, model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
			{
				if (getModelObject() == null)
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
		TextArea<T> editor = new TextArea<T>(parent, componentId, model);
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

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				final String saveCall = "{wicketAjaxGet('" + getCallbackUrl()
						+ "&save=true&'+this.name+'='+wicketEncode(this.value)); return true;}";

				final String cancelCall = "{wicketAjaxGet('" + getCallbackUrl()
						+ "&save=false'); return false;}";

				final String keypress = "var kc=wicketKeyCode(event); if (kc==27) " + cancelCall
						+ "; ";

				tag.put(ClientEvent.BLUR.getEvent(), saveCall);
				tag.put(ClientEvent.KEYPRESS.getEvent(), keypress);
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
	public final void setCols(int cols)
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
	public final void setRows(int rows)
	{
		this.rows = rows;
	}

	/**
	 * Override this to display a different value when the model object is null.
	 * Default is <code>...</code>
	 * 
	 * @return The string which should be displayed when the model object is
	 *         null.
	 */
	protected String defaultNullLabel()
	{
		return "...";
	}
}
