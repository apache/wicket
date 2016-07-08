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
package org.apache.wicket.examples.ajax.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.io.IClusterable;

/**
 * Ajax todo list without having to write any JavaScript yourself.
 * 
 * @author Martijn Dashorst
 */
public class TodoList extends BasePage
{
	/**
	 * The todo object.
	 */
	public static class TodoItem implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		/** Is the item done? */
		private boolean checked;

		/** Description of the item. */
		private String text;

		/** Constructor. */
		public TodoItem()
		{
		}

		/**
		 * Copy constructor.
		 * 
		 * @param item
		 *            the item to copy the values from.
		 */
		public TodoItem(TodoItem item)
		{
			text = item.text;
		}

		/**
		 * @return Returns the checked property.
		 */
		public boolean isChecked()
		{
			return checked;
		}

		/**
		 * Sets the checked property.
		 * 
		 * @param checked
		 *            The checked property to set.
		 */
		public void setChecked(boolean checked)
		{
			this.checked = checked;
		}

		/**
		 * Gets the description of the item.
		 * 
		 * @return Returns the text.
		 */
		public String getText()
		{
			return text;
		}

		/**
		 * Sets the description of the item.
		 * 
		 * @param text
		 *            The text to set.
		 */
		public void setText(String text)
		{
			this.text = text;
		}
	}

	/**
	 * Container for displaying the todo items in a list.
	 */
	public class TodoItemsContainer extends WebMarkupContainer
	{
		/**
		 * Constructor.
		 * 
		 * @param id
		 *            the component identifier.
		 */
		public TodoItemsContainer(String id)
		{
			super(id);

			// let wicket generate a markup-id so the contents can be
			// updated through an AJAX call.
			setOutputMarkupId(true);

			// add the listview to the container
			add(new ListView<TodoItem>("item", items)
			{
				@Override
				protected void populateItem(ListItem<TodoItem> item)
				{
					// add an AJAX checkbox to the item
					item.add(new AjaxCheckBox("check", new PropertyModel<>(
						item.getModel(), "checked"))
					{
						@Override
						protected void onUpdate(AjaxRequestTarget target)
						{
							// no need to do anything, the model is updated by
							// itself, and we don't have to re-render a
							// component (the client already has the correct
							// state).
						}
					});
					// display the text of the todo item
					item.add(new Label("text", new PropertyModel<String>(item.getModel(), "text")));
				}
			});
		}
	}

	/**
	 * Container for showing either the add link, or the addition form.
	 */
	private class AddItemsContainer extends WebMarkupContainer
	{
		/** Visibility toggle so that either the link or the form is visible. */
		private boolean linkVisible = true;

		/** Link for displaying the AddTodo form. */
		private final class AddTodoLink extends AjaxFallbackLink<Void>
		{
			/** Constructor. */
			private AddTodoLink(String id)
			{
				super(id);
			}

			/**
			 * onclick handler.
			 * 
			 * @param targetOptional
			 *            the request target.
			 */
			@Override
			public void onClick(Optional<AjaxRequestTarget> targetOptional)
			{
				onShowForm(targetOptional);
			}

			/**
			 * Toggles the visibility with the add form.
			 * 
			 * @return <code>true</code> when the add links is visible and the form isn't.
			 */
			@Override
			public boolean isVisible()
			{
				return linkVisible;
			}
		}

		/**
		 * Link for removing all completed todos from the list, this link follows the same
		 * visibility rules as the add link.
		 */
		private final class RemoveCompletedTodosLink extends AjaxFallbackLink<Void>
		{
			/**
			 * Constructor.
			 * 
			 * @param id
			 *            component id
			 */
			public RemoveCompletedTodosLink(String id)
			{
				super(id);
			}

			@Override
			public void onClick(Optional<AjaxRequestTarget> targetOptional)
			{
				onRemoveCompletedTodos(targetOptional);
			}

			/**
			 * Toggles the visibility with the add form.
			 * 
			 * @return <code>true</code> when the add links is visible and the form isn't.
			 */
			@Override
			public boolean isVisible()
			{
				return linkVisible;
			}
		}

		/**
		 * Displays a form which offers an edit field and two buttons: one for adding the todo item,
		 * and one for canceling the addition. The visibility of this component is mutual exclusive
		 * with the visibility of the add-link.
		 */
		private final class AddTodoForm extends Form<TodoItem>
		{
			/**
			 * Constructor.
			 * 
			 * @param id
			 *            the component id.
			 */
			public AddTodoForm(String id)
			{
				super(id, new CompoundPropertyModel<>(new TodoItem()));
				setOutputMarkupId(true);
				add(new TextField<>("text"));
				add(new AjaxButton("add", this)
				{
					@Override
					protected void onSubmit(AjaxRequestTarget target)
					{
						// retrieve the todo item
						TodoItem item = (TodoItem)getParent().getDefaultModelObject();

						// add the item
						onAdd(item, target);
					}

					@Override
					protected void onError(AjaxRequestTarget target)
					{
					}
				});

				add(new AjaxButton("cancel", this)
				{
					@Override
					public void onSubmit(AjaxRequestTarget target)
					{
						onCancelTodo(target);
					}

					@Override
					protected void onError(AjaxRequestTarget target)
					{
					}
				});
			}

			/**
			 * Toggles the visibility with the add link. When the link is visible, the form isn't.
			 * 
			 * @return true when the form is visible and the link isn't.
			 */
			@Override
			public boolean isVisible()
			{
				return !linkVisible;
			}
		}

		/**
		 * Constructor.
		 * 
		 * @param id
		 *            the component id.
		 */
		public AddItemsContainer(String id)
		{
			super(id);
			// let wicket generate a markup-id so the contents can be
			// updated through an AJAX call.
			setOutputMarkupId(true);
			add(new AddTodoLink("link"));
			add(new RemoveCompletedTodosLink("remove"));
			add(new AddTodoForm("form"));
		}

		/**
		 * Called then the add link was clicked, shows the form, and hides the link.
		 * 
		 * @param targetOptional
		 *            the request target.
		 */
		void onShowForm(Optional<AjaxRequestTarget> targetOptional)
		{
			// toggle the visibility
			linkVisible = false;

			// redraw the add container.
			targetOptional.ifPresent(target -> target.add(this));
		}

		void onRemoveCompletedTodos(Optional<AjaxRequestTarget> targetOptional)
		{
			List<TodoItem> ready = new ArrayList<>();
			for (TodoItem todo : items)
			{
				if (todo.isChecked())
				{
					ready.add(todo);
				}
			}
			items.removeAll(ready);

			targetOptional.ifPresent(target -> {
				// repaint our panel
				target.add(this);

				// repaint the listview as there was a new item added.
				target.add(showItems);
			});
		}

		/**
		 * Called when the form is submitted through the add button, stores the todo item, hides the
		 * form, displays the add link and updates the listview.
		 * 
		 * @param target
		 *            the request target
		 */
		void onAdd(TodoItem item, AjaxRequestTarget target)
		{
			// add the item
			items.add(new TodoItem(item));

			// reset the model
			item.setChecked(false);
			item.setText("");

			// toggle the visibility
			linkVisible = true;

			// repaint our panel
			target.add(this);

			// repaint the listview as there was a new item added.
			target.add(showItems);
		}

		/**
		 * Called when adding a new todo item was canceled. Hides the add form and displays the add
		 * link.
		 * 
		 * @param target
		 *            the request target.
		 */
		void onCancelTodo(AjaxRequestTarget target)
		{
			// toggle the visibility
			linkVisible = true;

			// repaint the panel.
			target.add(this);
		}
	}

	/**
	 * Container for redrawing the todo items list with an AJAX call.
	 */
	private final WebMarkupContainer showItems;

	/**
	 * The list of todo items.
	 */
	final List<TodoItem> items = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public TodoList()
	{
		// add the listview container for the todo items.
		showItems = new TodoItemsContainer("showItems");
		add(showItems);

		add(new AjaxFallbackLink<Void>("ajaxback")
		{
			@Override
			public void onClick(Optional<AjaxRequestTarget> targetOptional)
			{
				setResponsePage(getPage());
			}
		});
		// add the add container for the todo items.
		add(new AddItemsContainer("addItems"));
	}
}
