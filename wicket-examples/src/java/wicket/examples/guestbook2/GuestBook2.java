/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.guestbook2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import wicket.PageParameters;
import wicket.WicketRuntimeException;
import wicket.examples.WicketExamplePage;
import wicket.examples.util.hibernate.HibernateHelper;
import wicket.markup.html.basic.Label;
import wicket.markup.html.basic.MultiLineLabel;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextArea;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.MappingException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

/**
 * A simple "guest book" example that allows visitors to the page to add a
 * comment and see the comments others have added.
 * 
 * @author Jonathan Locke
 */
public class GuestBook2 extends WicketExamplePage
{
	/** A global list of all comments from all users */
	private static final List commentList = new ArrayList();

	/** The commentListView of comments shown on this page */
	private final ListView commentListView;

	static
	{
		try
		{
			try
			{
				new DBUtil().initDB("/hibernate.cfg.xml");
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}

			// Get hibernate session
			Session session = HibernateHelper.getSession();

			try
			{
				// Save comment to db
				commentList.clear();
				commentList.addAll(session
						.find("from comment in class wicket.examples.guestbook2.Comment "
								+ "order by comment.date desc"));
			}
			finally
			{
				HibernateHelper.closeSession();
			}
		}
		catch (MappingException e)
		{
			e.printStackTrace();
		}
		catch (HibernateException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public GuestBook2(final PageParameters parameters)
	{
		// Add comment form
		add(new CommentForm("commentForm"));

		// Add table of existing comments
		add(commentListView = new ListView("comments", commentList)
		{
			public void populateItem(final ListItem listItem)
			{
				final Comment comment = (Comment)listItem.getModelObject();
				listItem.add(new Label("date", comment.getDate()));
				listItem.add(new MultiLineLabel("text", comment.getText()));
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
		// The comment that this form is editing
		private final Comment comment = new Comment();

		/**
		 * Constructor.
		 * 
		 * @param componentName
		 *            The name of this component
		 */
		public CommentForm(final String componentName)
		{
			// Construct form with no validation listener
			super(componentName, null);

			// Add text entry widget
			add(new TextArea("text", comment, "text"));
		}

		/**
		 * Show the resulting valid edit.
		 */
		public final void handleSubmit()
		{
			// Construct a copy of the edited comment
			final Comment newComment = new Comment(comment);

			// Set date of comment to add
			newComment.setDate(new Date());

			// Add the component we edited to the list of comments
			commentList.add(0, newComment);

			try
			{
				// Get hibernate session
				Session session = HibernateHelper.getSession();

				try
				{
					// Save comment to db
					Transaction transaction = session.beginTransaction();
					session.save(newComment);
					transaction.commit();
				}
				finally
				{
					HibernateHelper.closeSession();
				}
			}
			catch (MappingException e)
			{
				throw new WicketRuntimeException("Unable to map hibernate object", e);
			}
			catch (HibernateException e)
			{
				throw new WicketRuntimeException("Unable to save comment", e);
			}

			// Tell list view that its model was changed
			commentListView.invalidateModel();
		}
	}
}


