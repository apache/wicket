/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.examples.guestbook;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.basic.MultiLineLabel;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextArea;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.CompoundPropertyModel;
import wicket.model.Model;

/**
 * A simple "guest book" example that allows visitors to the page to add a
 * comment and see the comments others have added.
 * 
 * For unit testing, added a parameter to clear the commentList.
 * 
 * @author Jonathan Locke
 * @author Martijn Dashorst
 */
public final class GuestBook extends WicketExamplePage
{
	/** A global list of all comments from all users across all sessions */
	private static final List commentList = new ArrayList();

	/** The list view that shows comments */
	private final ListView commentListView;

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 */
	public GuestBook()
	{
		// Add comment form
		add(new CommentForm("commentForm"));

		// Add commentListView of existing comments
		add(commentListView = new ListView("comments", commentList)
		{
			public void populateItem(final ListItem listItem)
			{
				final Comment comment = (Comment)listItem.getModelObject();
				listItem.add(new Label("date", new Model(comment.getDate())));
				listItem.add(new MultiLineLabel("text", comment.getText()));
			}
		}).setVersioned(false);
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
			add(new TextArea("text"));
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
			commentListView.modelChanging();
			commentList.add(0, newComment);
			commentListView.modelChanged();

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
