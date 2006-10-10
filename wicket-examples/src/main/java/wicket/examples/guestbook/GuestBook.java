/*
 * $Id$
 * $Revision$ $Date$
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

import wicket.MarkupContainer;
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
	/**
	 * A form that allows a user to add a comment.
	 * 
	 * @author Jonathan Locke
	 */
	public final class CommentForm extends Form<Comment>
	{
		/**
		 * Constructor
		 * 
		 * @param parent
		 *            The parent
		 * @param id
		 *            The name of this component
		 */
		public CommentForm(MarkupContainer parent, final String id)
		{
			// Construct form with no validation listener
			super(parent, id, new CompoundPropertyModel<Comment>(new Comment()));

			// Add text entry widget
			new TextArea(this, "text");
		}

		/**
		 * Show the resulting valid edit
		 */
		@Override
		public final void onSubmit()
		{
			// Construct a copy of the edited comment
			final Comment comment = getModelObject();
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

	/** A global list of all comments from all users across all sessions */
	private static final List<Comment> commentList = new ArrayList<Comment>();

	/**
	 * Clears the comments.
	 */
	public static void clear()
	{
		commentList.clear();
	}

	/** The list view that shows comments */
	private final ListView commentListView;

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 */
	public GuestBook()
	{
		// Add comment form
		new CommentForm(this, "commentForm");

		// Add commentListView of existing comments
		commentListView = (ListView)new ListView<Comment>(this, "comments", commentList)
		{
			@Override
			public void populateItem(final ListItem<Comment> listItem)
			{
				final Comment comment = listItem.getModelObject();
				new Label(listItem, "date", new Model<Date>(comment.getDate()));
				new MultiLineLabel(listItem, "text", comment.getText());
			}
		}.setVersioned(false);
	}
}
