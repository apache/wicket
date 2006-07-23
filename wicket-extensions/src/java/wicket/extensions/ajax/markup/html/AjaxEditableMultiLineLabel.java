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
import wicket.markup.ComponentTag;
import wicket.markup.html.basic.MultiLineLabel;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.TextArea;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;

/**
 * And inplace editor much like {@link AjaxEditableLabel}, but now with support
 * for multi line content and a {@link TextArea text area} as its editor.
 * 
 * @author eelcohillenius
 */
public class AjaxEditableMultiLineLabel extends AjaxEditableLabel
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
	public AjaxEditableMultiLineLabel(String id)
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
	public AjaxEditableMultiLineLabel(String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * @see wicket.extensions.ajax.markup.html.AjaxEditableLabel#newLabel(wicket.MarkupContainer, java.lang.String, wicket.model.IModel)
	 */
	protected Component newLabel(MarkupContainer parent, String componentId, IModel model)
	{
		MultiLineLabel label = new MultiLineLabel(componentId, model);
		label.setOutputMarkupId(true);
		label.add(new LabelAjaxBehavior("onclick"));
		return label;
	}

	/**
	 * @see wicket.extensions.ajax.markup.html.AjaxEditableLabel#newEditor(wicket.MarkupContainer, java.lang.String, wicket.model.IModel)
	 */
	protected FormComponent newEditor(MarkupContainer parent, String componentId, IModel model)
	{
		TextArea editor = new TextArea(componentId, model);
		editor.add(new AttributeModifier("rows", new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component c)
			{
				return new Integer(rows);
			}
		}));
		editor.add(new AttributeModifier("cols", new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component c)
			{
				return new Integer(cols);
			}
		}));
		editor.setOutputMarkupId(true);
		editor.setVisible(false);
		editor.add(new EditorAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see wicket.behavior.AbstractAjaxBehavior#onComponentTag(wicket.markup.ComponentTag)
			 */
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				final String saveCall = "{wicketAjaxGet('" + getCallbackUrl()
						+ "&save=true&'+this.name+'='+wicketEncode(this.value)); return true;}";

				final String cancelCall = "{wicketAjaxGet('" + getCallbackUrl()
						+ "&save=false'); return false;}";

				final String keypress = "var kc=wicketKeyCode(event); if (kc==27) " + cancelCall
						+ "; ";

				tag.put("onblur", saveCall);
				tag.put("onkeypress", keypress);
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
}
