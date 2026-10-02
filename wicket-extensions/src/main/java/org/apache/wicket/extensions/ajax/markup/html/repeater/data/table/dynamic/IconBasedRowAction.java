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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.floating.FloatingPanel;
import org.apache.wicket.extensions.markup.html.icon.IIcon;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * A row action shown as an icon button, its tooltip naming what it does, optionally asking for a
 * confirmation before it acts.
 * <p>
 * The button carries the {@link #getActionId() action}, so a click is dispatched by the
 * {@link DynamicDataTable} the action is registered with, through a {@link CompoundDynamicColumn}
 * or {@link DynamicDataTable#addActionContributor(IAjaxActionColumnContributor)}. The tooltip is
 * written escaped, as the button's {@code title} and {@code aria-label}.
 * <p>
 * With a {@link #setConfirmation(IModel) confirmation}, a click first shows the question with a
 * confirm and a cancel button, in a {@link FloatingPanel} titled with the tooltip, in the table's {@link DynamicDataTable#showOverlay(org.apache.wicket.Component,
 * org.apache.wicket.core.request.handler.IPartialPageRequestHandler) overlay}, which blocks the
 * table until the user answers. {@link #onRowAction(Object, AjaxRequestTarget)} is called only
 * when the user confirms and the row still exists.
 *
 * @param <T>
 *            the type of the rows
 * @since 11.0.0
 */
public abstract class IconBasedRowAction<T> implements IAjaxActionColumnContributor<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * The icons shipped with the row actions, drawn in the text color of the button. Any other
	 * {@link IIcon}, such as an {@link org.apache.wicket.extensions.markup.html.icon.SvgIcon},
	 * works as well.
	 */
	public enum Icon implements IIcon
	{
		/** A pencil. */
		EDIT("M4.5 19.5h4l11-11-4-4-11 11z M14 6l4 4"),

		/** A waste bin. */
		DELETE("M4 7h16 M9 7V4h6v3 M6 7l1 13h10l1-13 M10 11v6 M14 11v6"),

		/** An arrow pointing down onto a line. */
		DOWNLOAD("M12 3v12 M7 10l5 5 5-5 M5 20h14"),

		/** A plus sign. */
		ADD("M12 5v14 M5 12h14");

		private final String path;

		Icon(String path)
		{
			this.path = path;
		}

		/**
		 * @return the icon as inline SVG markup
		 */
		@Override
		public String getMarkup()
		{
			return "<svg aria-hidden=\"true\" focusable=\"false\" viewBox=\"0 0 24 24\" " +
				"width=\"18\" height=\"18\" fill=\"none\" stroke=\"currentColor\" " +
				"stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"" +
				path + "\"/></svg>";
		}
	}

	private final String actionId;

	private final IIcon icon;

	private final IModel<String> tooltip;

	private IModel<String> confirmation;

	private DynamicDataTable<T, ?> table;

	/**
	 * @param actionId
	 *            the action the button carries, unique within its table
	 * @param icon
	 *            the icon of the button
	 * @param tooltip
	 *            what the action does, shown as the button's tooltip and read by screen readers
	 */
	public IconBasedRowAction(String actionId, IIcon icon, IModel<String> tooltip)
	{
		this.actionId = Args.notEmpty(actionId, "actionId");
		this.icon = Args.notNull(icon, "icon");
		this.tooltip = Args.notNull(tooltip, "tooltip");
	}

	@Override
	public String getActionId()
	{
		return actionId;
	}

	/**
	 * Returns the icon of the button, the one given to the constructor by default. Override to
	 * choose it when the template is rendered.
	 *
	 * @return the icon of the button
	 */
	public IIcon getIcon()
	{
		return icon;
	}

	/**
	 * Asks the user to confirm before the action is carried out, for example before a row is
	 * deleted.
	 *
	 * @param confirmation
	 *            the question to confirm, written escaped; {@code null} to act without asking
	 * @return {@code this}
	 */
	public IconBasedRowAction<T> setConfirmation(IModel<String> confirmation)
	{
		this.confirmation = confirmation;
		return this;
	}

	/**
	 * Returns the question to confirm before acting on a row, the one set with
	 * {@link #setConfirmation(IModel)} by default. Override to ask about the row itself.
	 *
	 * @param row
	 *            the row the action was clicked on
	 * @return the question, or {@code null} to act without asking
	 */
	protected IModel<String> getConfirmation(T row)
	{
		return confirmation;
	}

	/**
	 * Writes the button, with the tooltip escaped.
	 */
	@Override
	public String getTemplate()
	{
		return button(actionId, getIcon().getMarkup(), tooltip.getObject());
	}

	/**
	 * Like {@link #getTemplate()}, with a tooltip that is an {@link IComponentAssignedModel}, such
	 * as a {@link org.apache.wicket.model.ResourceModel}, resolved against the table.
	 */
	@Override
	public String getTemplate(DynamicDataTable<T, ?> table)
	{
		return button(actionId, getIcon().getMarkup(), resolve(tooltip, table));
	}

	@Override
	public void bind(DynamicDataTable<T, ?> table)
	{
		this.table = table;
	}

	/**
	 * @return the table the action is registered with, or {@code null} before it is registered
	 */
	protected DynamicDataTable<T, ?> getTable()
	{
		return table;
	}

	/**
	 * Carries out the action, or asks for a confirmation first if there is one.
	 */
	@Override
	public final void onAction(T row, AjaxRequestTarget target)
	{
		IModel<String> question = getConfirmation(row);
		if (question == null)
		{
			onRowAction(row, target);
		}
		else
		{
			String key = table.getRowKey(row);
			table.showOverlay(new FloatingPanel(DynamicDataTable.OVERLAY_CONTENT_ID, tooltip)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected Component newBody(String id)
				{
					return new RowActionConfirmationPanel<>(id, question, key,
						IconBasedRowAction.this);
				}

				@Override
				protected void onClose(AjaxRequestTarget target)
				{
					table.closeOverlay(target);
				}
			}, target);
		}
	}

	/**
	 * Carries out the action on a row: right away, or once the user confirmed it.
	 *
	 * @param row
	 *            the row, never {@code null}
	 * @param target
	 *            the Ajax request target
	 */
	protected abstract void onRowAction(T row, AjaxRequestTarget target);

	@Override
	public void detach()
	{
		tooltip.detach();
		if (confirmation != null)
		{
			confirmation.detach();
		}
	}

	/**
	 * @return the markup of an icon button carrying the action, with the tooltip escaped
	 */
	static String button(String actionId, String iconMarkup, String tooltip)
	{
		String escaped = Strings.escapeMarkup(tooltip != null ? tooltip : "").toString();
		return "<button type=\"button\" class=\"dynamic-data-table-action " +
			"dynamic-data-table-icon-action\" data-dt-action=\"" + Strings.escapeMarkup(actionId) +
			"\" title=\"" + escaped + "\" aria-label=\"" + escaped + "\">" + iconMarkup +
			"</button>";
	}

	/**
	 * @return the object of the model, resolved against the table if it is an
	 *         {@link IComponentAssignedModel}
	 */
	static String resolve(IModel<String> model, DynamicDataTable<?, ?> table)
	{
		if (model instanceof IComponentAssignedModel<String> assigned)
		{
			IModel<String> wrapped = assigned.wrapOnAssignment(table);
			try
			{
				return wrapped.getObject();
			}
			finally
			{
				wrapped.detach();
			}
		}
		return model.getObject();
	}
}
