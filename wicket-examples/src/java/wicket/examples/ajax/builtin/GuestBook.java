package wicket.examples.ajax.builtin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import wicket.Component;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.calldecorator.AjaxCallDecorator;
import wicket.ajax.form.AjaxFormSubmitBehavior;
import wicket.examples.guestbook.Comment;
import wicket.examples.guestbook.GuestBook.CommentForm;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.basic.MultiLineLabel;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextArea;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.CompoundPropertyModel;
import wicket.model.Model;
import wicket.model.PropertyModel;

public class GuestBook extends BasePage
{
	/** A global list of all comments from all users across all sessions */
	public static final List commentList = new ArrayList();

	/** The list view that shows comments */
	private final ListView commentListView;
	private WebMarkupContainer comments;
	private Component text;


	public GuestBook()
	{
		// Add comment form
		CommentForm commentForm = new CommentForm("commentForm");
		add(commentForm);

		comments = new WebMarkupContainer("comments");
		add(comments.setOutputMarkupId(true));
		// Add commentListView of existing comments
		comments.add(commentListView = new ListView("comments", new PropertyModel(this, "commentList"))
		{
			public void populateItem(final ListItem listItem)
			{
				final Comment comment = (Comment)listItem.getModelObject();
				listItem.add(new Label("date", new Model(comment.getDate())));
				listItem.add(new MultiLineLabel("text", comment.getText()));
			}
		});
		commentForm.add(new AjaxFormSubmitBehavior(commentForm, "onsubmit")
		{
			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return new AjaxCallDecorator()
				{
					public CharSequence decorateScript(CharSequence script)
					{
						return script + "return false;";
					}
				};
			}

			protected void onSubmit(AjaxRequestTarget target)
			{
				target.addComponent(comments);
				target.addComponent(text);
				// focus the textarea again
				target.addJavascript("document.getElementById('" + text.getMarkupId() + "').focus();");
			}
		});
	}

	/**
	 * A form that allows a user to add a comment.
	 * 
	 * @author Jonathan Locke
	 */
	public final class CommentForm extends Form
	{
		/**
		 * Constructor
		 * 
		 * @param id
		 *            The name of this component
		 */
		public CommentForm(final String id)
		{
			// Construct form with no validation listener
			super(id, new CompoundPropertyModel(new Comment()));

			// Add text entry widget
			text = new TextArea("text").setOutputMarkupId(true);
			add(text);
		}

		/**
		 * Show the resulting valid edit
		 */
		public final void onSubmit()
		{
			// Construct a copy of the edited comment
			final Comment comment = (Comment)getModelObject();
			final Comment newComment = new Comment(comment);

			// Set date of comment to add
			newComment.setDate(new Date());

			// Add the component we edited to the list of comments
			commentList.add(0, newComment);

			// Clear out the text component
			comment.setText("");
		}
	}

	/**
	 * Clears the comments.
	 */
	public static void clear()
	{
		commentList.clear();
	}
}
