///////////////////////////////////////////////////////////////////////////////////
//
// Created May 21, 2004
//
// Copyright 2004, Jonathan W. Locke
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package library;

import java.util.ArrayList;
import java.util.List;

import com.voicetribe.util.lang.EnumeratedType;
import com.voicetribe.wicket.Model;
import com.voicetribe.wicket.Page;
import com.voicetribe.wicket.PropertyModel;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.form.CheckBox;
import com.voicetribe.wicket.markup.html.form.DropDownChoice;
import com.voicetribe.wicket.markup.html.form.Form;
import com.voicetribe.wicket.markup.html.form.FormComponentFeedbackBorder;
import com.voicetribe.wicket.markup.html.form.ListMultipleChoice;
import com.voicetribe.wicket.markup.html.form.RadioChoice;
import com.voicetribe.wicket.markup.html.form.RadioOption;
import com.voicetribe.wicket.markup.html.form.RadioOptionSet;
import com.voicetribe.wicket.markup.html.form.TextField;
import com.voicetribe.wicket.markup.html.form.validation.LengthValidator;
import com.voicetribe.wicket.markup.html.form.validation.RequiredValidator;
import com.voicetribe.wicket.markup.html.link.IPageLink;
import com.voicetribe.wicket.markup.html.link.PageLink;
import com.voicetribe.wicket.markup.html.panel.FeedbackPanel;

/**
 * A page that contains a form that allows editing of books.
 * @author Jonathan Locke
 */
public final class EditBook extends AuthenticatedHtmlPage
{
    /**
     * Constructs a page that edits a book
     * @param book The book to edit
     */
    public EditBook(final Book book)
    {
        // Create and add feedback panel to page
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(feedback);

        // Add edit book form to page
        add(new EditBookForm("editBookForm", book, feedback));
    }

    /**
     * Form that edits a book
     * @author Jonathan Locke
     */
    static public final class EditBookForm extends Form
    {
        /**
         * Constructor
         * @param componentName Name of form
         * @param book Book model
         * @param feedback Feedback component that shows errors
         */
        public EditBookForm(final String componentName, final Book book, final FeedbackPanel feedback)
        {
            super(componentName, feedback);

            // Set model
            this.book = book;
            Model bookModel = new Model(book);
            setModel(new PropertyModel(bookModel, getName()));

            // Create a required text field that edits the book's author
            final TextField author = new TextField("author",
                    new PropertyModel(bookModel, "author"));
            author.add(new RequiredValidator());
            add(author);

            // Create a required text field with a max length of 30 characters that edits the book's title
            final TextField title = new TextField("title",
                    new PropertyModel(bookModel, "title"));
            title.add(new RequiredValidator());
            title.add(LengthValidator.max(30));
            final FormComponentFeedbackBorder titleFeedback =
                new FormComponentFeedbackBorder("titleFeedback");
            titleFeedback.add(title);
            add(titleFeedback);

            // Add fiction checkbox
            add(new CheckBox("fiction", new PropertyModel(bookModel, "fiction")));

            // Books is everything but otherBook
            List books = new ArrayList();
            books.addAll(Book.getBooks());
            books.remove(otherBook);

            // Add companion book choice
            add(new DropDownChoice("companionBook",
                    new PropertyModel(bookModel, "companionBook"), books));

            // Add radio choice test
            final RadioChoice relatedBook = new RadioChoice(
                    "relatedBook", new PropertyModel(bookModel, "relatedBook"));
            relatedBook.add(new RadioOptionSet("relatedBooks", books));
            relatedBook.add(new RadioOption("otherBook", otherBook));
            add(relatedBook);

            // Multi-select among writing styles
            add(new ListMultipleChoice("writingStyles",
                    new PropertyModel(bookModel, "writingStyles"),
                    EnumeratedType.getValues(Book.WritingStyle.class)));
        }

        /**
         * Show the resulting valid edit
         * @param cycle The request cycle
         */
        public final void handleSubmit(final RequestCycle cycle)
        {
            // Go to details page for book
            BookDetails.setPage(cycle, book);
        }
        
        private Book book;
    }

    /**
     * Gets a link to a page that will edit a book
     * @param name The name of the link
     * @param id The id of the book that the page will edit
     * @return The page link
     */
    public static PageLink link(final String name, final long id)
    {
        return new PageLink(name, new IPageLink()
        {
            public Page getPage()
            {
                return new EditBook(Book.get(id));
            }

            public Class getPageClass()
            {
                return EditBook.class;
            }
        });
    }

    static final Book otherBook = new Book("Frisbee Techniques", "Marty van Hoff", Book.FICTION);
}

///////////////////////////////// End of File /////////////////////////////////
