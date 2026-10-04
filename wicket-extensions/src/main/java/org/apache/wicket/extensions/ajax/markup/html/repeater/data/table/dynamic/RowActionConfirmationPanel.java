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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

/**
 * Asks to confirm an {@link IconBasedRowAction} in the overlay of its table, and carries out the
 * action on the row, looked up again by its key, once the user confirms.
 *
 * @param <T>
 *            the type of the rows
 */
class RowActionConfirmationPanel<T> extends Panel
{
	private static final long serialVersionUID = 1L;

	RowActionConfirmationPanel(String id, IModel<String> question, String rowKey,
		IconBasedRowAction<T> action)
	{
		super(id);

		add(new Label("question", question));
		add(new AjaxLink<Void>("cancel")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				action.getTable().closeOverlay(target);
			}
		}.setBody(new ResourceModel("DynamicDataTable.cancel", "Cancel")));
		add(new AjaxLink<Void>("confirm")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				DynamicDataTable<T, ?> table = action.getTable();
				table.closeOverlay(target);
				T row = table.findRow(rowKey);
				if (row != null)
				{
					action.onRowAction(row, target);
				}
			}
		}.setBody(new ResourceModel("DynamicDataTable.confirm", "Confirm")));
	}
}
