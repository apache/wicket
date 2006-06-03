package wicket.examples.ajax.builtin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import wicket.MarkupContainer;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.ajax.markup.html.form.AjaxCheckBox;
import wicket.ajax.markup.html.form.AjaxSubmitButton;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.CompoundPropertyModel;
import wicket.model.PropertyModel;

/**
 * Ajax todo list without having to write any JavaScript yourself.
 * 
 * @author Martijn Dashorst
 */
public class TodoList extends BasePage
{
	/**
	 * Container for showing either the add link, or the addition form.
	 */
	public class AddItemsContainer extends WebMarkupContainer
	{
		/**
		 * Displays a form which offers an edit field and two buttons: one for
		 * adding the todo item, and one for canceling the addition. The
		 * visibility of this component is mutual exclusive with the visibility
		 * of the add-link.
		 */
		private final class AddTodoForm extends Form<TodoItem>
		{
			/**
			 * Constructor.
			 * 
			 * @param parent
			 *            The parent
			 * 
			 * @param id
			 *            the component id.
			 */
			public AddTodoForm(MarkupContainer parent, String id)
			{
				super(parent, id, new CompoundPropertyModel<TodoItem>(new TodoItem()));
				setOutputMarkupId(true);
				new TextField(this, "text");
				new AjaxSubmitButton(this, "add", this)
				{
					@Override
					protected void onSubmit(AjaxRequestTarget target, Form form)
					{
						// retrieve the todo item
						TodoItem item = (TodoItem)getParent().getModelObject();

						// add the item
						onAdd(item, target);
					}
				};

				new AjaxSubmitButton(this, "cancel", this)
				{
					@Override
					public void onSubmit(AjaxRequestTarget target, Form form)
					{
						onCancelTodo(target);
					}
				};
			}

			/**
			 * Toggles the visibility with the add link. When the link is
			 * visible, the form isn't.
			 * 
			 * @return true when the form is visible and the link isn't.
			 */
			@Override
			public boolean isVisible()
			{
				return !linkVisible;
			}
		}

		/** Link for displaying the AddTodo form. */
		private final class AddTodoLink extends AjaxFallbackLink
		{
			/** Constructor. */
			private AddTodoLink(MarkupContainer parent, String id)
			{
				super(parent, id);
			}

			/**
			 * Toggles the visibility with the add form.
			 * 
			 * @return <code>true</code> when the add links is visible and the
			 *         form isn't.
			 */
			@Override
			public boolean isVisible()
			{
				return linkVisible;
			}

			/**
			 * onclick handler.
			 * 
			 * @param target
			 *            the request target.
			 */
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				onShowForm(target);
			}
		}

		/**
		 * Link for removing all completed todos from the list, this link
		 * follows the same visibility rules as the add link.
		 */
		private final class RemoveCompletedTodosLink extends AjaxFallbackLink
		{
			/**
			 * Constructor.
			 * 
			 * @param parent
			 * 
			 * @param id
			 *            component id
			 */
			public RemoveCompletedTodosLink(MarkupContainer parent, String id)
			{
				super(parent, id);
			}

			/**
			 * Toggles the visibility with the add form.
			 * 
			 * @return <code>true</code> when the add links is visible and the
			 *         form isn't.
			 */
			@Override
			public boolean isVisible()
			{
				return linkVisible;
			}

			/**
			 * @see AjaxFallbackLink#onClick(AjaxRequestTarget)
			 */
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				onRemoveCompletedTodos(target);
			}
		}

		/** Visibility toggle so that either the link or the form is visible. */
		private boolean linkVisible = true;

		/**
		 * Constructor.
		 * 
		 * @param parent
		 * 
		 * @param id
		 *            the component id.
		 */
		public AddItemsContainer(MarkupContainer parent, String id)
		{
			super(parent, id);
			// let wicket generate a markup-id so the contents can be
			// updated through an AJAX call.
			setOutputMarkupId(true);
			new AddTodoLink(this, "link");
			new RemoveCompletedTodosLink(this, "remove");
			new AddTodoForm(this, "form");
		}

		/**
		 * Called when the form is submitted through the add button, stores the
		 * todo item, hides the form, displays the add link and updates the
		 * listview.
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
			target.addComponent(this);

			// repaint the listview as there was a new item added.
			target.addComponent(showItems);
		}

		/**
		 * Called when adding a new todo item was canceled. Hides the add form
		 * and displays the add link.
		 * 
		 * @param target
		 *            the request target.
		 */
		void onCancelTodo(AjaxRequestTarget target)
		{
			// toggle the visibility
			linkVisible = true;

			// repaint the panel.
			target.addComponent(this);
		}

		void onRemoveCompletedTodos(AjaxRequestTarget target)
		{
			List<TodoItem> ready = new ArrayList<TodoItem>();
			for (TodoItem todo : items)
			{
				if (todo.isChecked())
				{
					ready.add(todo);
				}
			}
			items.removeAll(ready);

			// repaint our panel
			target.addComponent(this);

			// repaint the listview as there was a new item added.
			target.addComponent(showItems);
		}

		/**
		 * Called then the add link was clicked, shows the form, and hides the
		 * link.
		 * 
		 * @param target
		 *            the request target.
		 */
		void onShowForm(AjaxRequestTarget target)
		{
			// toggle the visibility
			linkVisible = false;

			// redraw the add container.
			target.addComponent(this);
		}
	}

	/**
	 * The todo object.
	 */
	public static class TodoItem implements Serializable
	{
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
			this.text = item.text;
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
		 * @param parent
		 *            The parent Component
		 * 
		 * @param id
		 *            the component identifier.
		 */
		public TodoItemsContainer(MarkupContainer parent, String id)
		{
			super(parent, id);

			// let wicket generate a markup-id so the contents can be
			// updated through an AJAX call.
			setOutputMarkupId(true);

			// add the listview to the container
			new ListView<TodoItem>(this, "item", items)
			{
				@Override
				protected void populateItem(ListItem item)
				{
					// add an AJAX checkbox to the item
					new AjaxCheckBox(item, "check", new PropertyModel<Boolean>(item.getModel(), "checked"))
					{
						@Override
						protected void onUpdate(AjaxRequestTarget target)
						{
							// no need to do anything, the model is updated by
							// itself, and we don't have to re-render a
							// component (the client already has the correct
							// state).
						}
					};
					// display the text of the todo item
					new Label(item, "text", new PropertyModel(item.getModel(), "text"));
				}
			};
		}
	}

	/**
	 * The list of todo items.
	 */
	static final List<TodoItem> items = new ArrayList<TodoItem>();

	/**
	 * Container for redrawing the todo items list with an AJAX call.
	 */
	private WebMarkupContainer showItems;

	/**
	 * Constructor.
	 */
	public TodoList()
	{
		// add the listview container for the todo items.
		showItems = new TodoItemsContainer(this, "showItems");

		// add the add container for the todo items.
		new AddItemsContainer(this, "addItems");
	}
}
