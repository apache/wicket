package wicket.examples.ajax.builtin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
			add(new ListView("item", items)
			{
				protected void populateItem(ListItem item)
				{
					// add an AJAX checkbox to the item
					item.add(new AjaxCheckBox("check",
							new PropertyModel(item.getModel(), "checked"))
					{
						protected void onUpdate(AjaxRequestTarget target)
						{
							// no need to do anything, the model is updated by
							// itself, and we don't have to re-render a
							// component (the client already has the correct
							// state).
						}
					});
					// display the text of the todo item
					item.add(new Label("text", new PropertyModel(item.getModel(), "text")));
				}
			});
		}
	}

	/**
	 * Container for showing either the add link, or the addition form.
	 */
	public class AddItemsContainer extends WebMarkupContainer
	{
		/** Visibility toggle so that either the link or the form is visible. */
		private boolean linkVisible = true;

		/** Link for displaying the AddTodo form. */
		private final class AddTodoLink extends AjaxFallbackLink
		{
			/** Constructor. */
			private AddTodoLink(String id)
			{
				super(id);
			}

			/**
			 * onclick handler.
			 * 
			 * @param target
			 *            the request target.
			 */
			public void onClick(AjaxRequestTarget target)
			{
				onShowForm(target);
			}

			/**
			 * Toggles the visibility with the add form.
			 * 
			 * @return <code>true</code> when the add links is visible and the
			 *         form isn't.
			 */
			public boolean isVisible()
			{
				return linkVisible;
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
			 * @param id
			 *            component id
			 */
			public RemoveCompletedTodosLink(String id)
			{
				super(id);
			}

			/**
			 * @see AjaxFallbackLink#onClick(AjaxRequestTarget)
			 */
			public void onClick(AjaxRequestTarget target)
			{
				onRemoveCompletedTodos(target);
			}

			/**
			 * Toggles the visibility with the add form.
			 * 
			 * @return <code>true</code> when the add links is visible and the
			 *         form isn't.
			 */
			public boolean isVisible()
			{
				return linkVisible;
			}
		}
		
		/**
		 * Displays a form which offers an edit field and two buttons: one for
		 * adding the todo item, and one for canceling the addition. The
		 * visibility of this component is mutual exclusive with the visibility
		 * of the add-link.
		 */
		private final class AddTodoForm extends Form
		{
			/**
			 * Constructor.
			 * 
			 * @param id
			 *            the component id.
			 */
			public AddTodoForm(String id)
			{
				super(id, new CompoundPropertyModel(new TodoItem()));
				setOutputMarkupId(true);
				add(new TextField("text"));
				add(new AjaxSubmitButton("add", this)
				{
					protected void onSubmit(AjaxRequestTarget target, Form form)
					{
						// retrieve the todo item
						TodoItem item = (TodoItem)getParent().getModelObject();

						// add the item
						onAdd(item, target);
					}
				});
				
				add(new AjaxSubmitButton("cancel", this)
				{
					public void onSubmit(AjaxRequestTarget target, Form form)
					{
						onCancelTodo(target);
					}
				});
			}

			/**
			 * Toggles the visibility with the add link. When the link is
			 * visible, the form isn't.
			 * 
			 * @return true when the form is visible and the link isn't.
			 */
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

		void onRemoveCompletedTodos(AjaxRequestTarget target)
		{
			List ready = new ArrayList();
			for (Iterator iter = items.iterator(); iter.hasNext();)
			{
				TodoItem todo = (TodoItem)iter.next();
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
	}

	/**
	 * Container for redrawing the todo items list with an AJAX call.
	 */
	private WebMarkupContainer showItems;

	/**
	 * The list of todo items.
	 */
	static final List items = new ArrayList();

	/**
	 * Constructor.
	 */
	public TodoList()
	{
		// add the listview container for the todo items.
		showItems = new TodoItemsContainer("showItems");
		add(showItems);

		// add the add container for the todo items.
		add(new AddItemsContainer("addItems"));
	}
}
