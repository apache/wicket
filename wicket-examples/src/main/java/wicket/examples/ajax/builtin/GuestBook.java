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

/**
 * Ajax enabled example for the guestbook.
 * 
 * @author Martijn Dashorst
 */
public class GuestBook extends BasePage
{
	/** A global list of all comments from all users across all sessions */
	public static final List commentList = new ArrayList();

	/** The list view that shows comments */
	private final ListView commentListView;
	/** Container for the comments, used to update the listview.  */
	private WebMarkupContainer comments;
	
	/** The textarea for entering the comments, is updated in the ajax call. */
	private Component text;

	/**
	 * Constructor.
	 */
	public GuestBook()
	{
		// Add comment form
		CommentForm commentForm = new CommentForm("commentForm");
		add(commentForm);

		// the WebMarkupContainer is used to update the listview in an ajax call
		comments = new WebMarkupContainer("comments");
		add(comments.setOutputMarkupId(true));
		
		// Add commentListView of existing comments
		comments.add(commentListView = new ListView("comments", new PropertyModel(this,
				"commentList"))
		{
			public void populateItem(final ListItem listItem)
			{
				final Comment comment = (Comment)listItem.getModelObject();
				listItem.add(new Label("date", new Model(comment.getDate())));
				listItem.add(new MultiLineLabel("text", comment.getText()));
			}
		});
		
		// we need to cancel the standard submit of the form in the onsubmit handler,
		// otherwise we'll get double submits. To do so, we return false after the
		// ajax submit has occurred.
		
		// The AjaxFormSubmitBehavior already calls the onSubmit of the form, all
		// we need to do in the onSubmit(AjaxRequestTarget) handler is do our Ajax
		// specific stuff, like rendering our components.
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
				// add the list of components that need to be updated
				target.addComponent(comments);
				target.addComponent(text);
				
				// focus the textarea again
				target.appendJavascript("document.getElementById('" + text.getMarkupId()
						+ "').focus();");
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
