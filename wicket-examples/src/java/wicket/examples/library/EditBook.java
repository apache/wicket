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
package wicket.examples.library;


import java.util.ArrayList;
import java.util.List;

import wicket.MarkupContainer;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.ListMultipleChoice;
import wicket.markup.html.form.RadioChoice;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.FormComponentFeedbackBorder;
import wicket.markup.html.form.validation.StringValidator;
import wicket.markup.html.link.IPageLink;
import wicket.markup.html.link.PageLink;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;
import wicket.util.lang.EnumeratedType;

/**
 * A page that contains a form that allows editing of books.
 * 
 * @author Jonathan Locke
 */
public final class EditBook extends AuthenticatedWebPage
{
	static final Book otherBook = new Book("Frisbee Techniques", "Marty van Hoff", Book.FICTION);

	/**
	 * Constructs a page that edits a book
	 * 
	 * @param book
	 *            The book to edit
	 */
	public EditBook(final Book book)
	{
		// Create and add feedback panel to page
		final FeedbackPanel feedback = new FeedbackPanel(this,"feedback");

		add(feedback);

		// Add edit book form to page
		add(new EditBookForm(this,"editBookForm", book));
	}

	/**
	 * Gets a link to a page that will edit a book
	 * 
	 * @param name
	 *            The name of the link
	 * @param id
	 *            The id of the book that the page will edit
	 * @return The page link
	 */
	public static PageLink link(MarkupContainer parent, final String name, final long id)
	{
		return new PageLink(parent,name, new IPageLink()
		{
			public Page getPage()
			{
				return new EditBook(Book.get(id));
			}

			public Class getPageIdentity()
			{
				return EditBook.class;
			}
		});
	}

	/**
	 * Form that edits a book
	 * 
	 * @author Jonathan Locke
	 */
	static public final class EditBookForm extends Form
	{
		/**
		 * Constructor
		 * 
		 * @param id
		 *            id of form
		 * @param book
		 *            Book model
		 */
		public EditBookForm(MarkupContainer parent,final String id, final Book book)
		{
			super(parent,id, new CompoundPropertyModel(book));

			// Create a required text field with a max length of 30 characters
			// that edits the book's title
			final FormComponentFeedbackBorder titleFeedback = new FormComponentFeedbackBorder(this,
			"titleFeedback");
			final TextField title = new TextField(titleFeedback,"title");
			title.setRequired(true);
			title.add(StringValidator.maximumLength(30));
			add(titleFeedback);
			titleFeedback.add(title);

			// Create a required text field that edits the book's author
			final FormComponentFeedbackBorder authorFeedback = new FormComponentFeedbackBorder(this,
			"authorFeedback");
			final TextField author = new TextField(authorFeedback,"author");
			author.setRequired(true);
			add(authorFeedback);
			authorFeedback.add(author);

			// Add fiction checkbox
			add(new CheckBox(this,"fiction"));

			// Books is everything but otherBook
			List books = new ArrayList();

			books.addAll(Book.getBooks());
			books.remove(otherBook);

			// Add companion book choice
			add(new DropDownChoice(this,"companionBook", books));

			// Add radio choice test
			final RadioChoice relatedBook = new RadioChoice(this,"relatedBook", books);
			add(relatedBook);

			// Multi-select among writing styles
			add(new ListMultipleChoice(this,"writingStyles", EnumeratedType
					.getValues(Book.WritingStyle.class)));
		}

		/**
		 * Show the resulting valid edit
		 */
		public final void onSubmit()
		{
			final RequestCycle cycle = getRequestCycle();
			PageParameters parameters = new PageParameters();
			final Book book = (Book)getModelObject();
			parameters.put("id", new Long(book.getId()));
			cycle.setResponsePage(getPageFactory().newPage(BookDetails.class, parameters));
			cycle.setRedirect(true);
		}
	}
}
