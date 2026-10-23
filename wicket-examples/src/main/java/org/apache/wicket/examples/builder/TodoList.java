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
package org.apache.wicket.examples.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.examples.ajax.builtin.TodoList.TodoItem;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * The todo list of {@link org.apache.wicket.examples.ajax.builtin.TodoList}, built.
 */
public class TodoList extends BasePage
{
	private static final long serialVersionUID = 1L;

	private final List<TodoItem> items = new ArrayList<>();

	public TodoList()
	{
		IModel<Boolean> adding = Model.of(false);
		IModel<Boolean> browsing = adding.map(a -> !a);
		IModel<TodoItem> draft = Model.of(new TodoItem());

		WebMarkupContainer showItems = container("showItems")
			.outputMarkupId()
			.body(container -> container.add(list("item", () -> items)
				.each(row -> row.add(
					ajaxCheckBox("check", row.bind(TodoItem::isChecked, TodoItem::setChecked)),
					label("text", row.bind(TodoItem::getText))))))
			.build();
		add(showItems);

		add(ajaxLink("ajaxback").onClick(target -> setResponsePage(getPage())),
			container("addItems")
				.outputMarkupId()
				.body(addItems -> addItems.add(
					ajaxLink("link")
						.visibleWhen(browsing)
						.onClick(target -> {
							adding.setObject(true);
							target.add(addItems.self());
						}),
					ajaxLink("remove")
						.visibleWhen(browsing)
						.onClick(target -> {
							items.removeIf(TodoItem::isChecked);
							target.add(addItems.self(), showItems);
						}),
					form("form", draft)
						.visibleWhen(adding)
						.body(form -> form.add(
							textField("text", form.bind(TodoItem::getText, TodoItem::setText)),
							ajaxButton("add").onSubmit(target -> {
								items.add(new TodoItem(draft.getObject()));
								draft.setObject(new TodoItem());
								adding.setObject(false);
								target.add(addItems.self(), showItems);
							}),
							ajaxButton("cancel").onSubmit(target -> {
								adding.setObject(false);
								target.add(addItems.self());
							}))))));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
			org.apache.wicket.examples.ajax.builtin.TodoList.class, "todo.css")));
	}
}
