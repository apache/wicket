/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package library;

import com.voicetribe.util.string.StringList;
import com.voicetribe.util.string.StringValueConversionException;
import com.voicetribe.wicket.Model;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.link.ExternalPageLink;
import com.voicetribe.wicket.markup.html.style.Italic;

import java.util.Iterator;

/**
 * A book details page.  Shows information about a book.
 * @author Jonathan Locke
 */
public final class BookDetails extends AuthenticatedHtmlPage
{
    /**
     * Constructor for calls from external page links
     * @param parameters Page parameters
     * @throws StringValueConversionException
     */
    public BookDetails(final PageParameters parameters)
        throws StringValueConversionException
    {
        this(Book.get(parameters.getLong("id")));
    }

    /**
     * Constructor
     * @param book The model
     */
    public BookDetails(final Book book)
    {
        Model bookModel = new Model(book);

        add(new Label("title", book.getTitle()));
        add(new Label("author", book.getAuthor()));
        add(new Label("fiction", Boolean.toString(book.getFiction())));
        add(BookDetails.link("companion", book.getCompanionBook(),
                getLocalizedString("noBookTitle")));
        add(BookDetails.link("related", book.getRelatedBook(),
                getLocalizedString("noBookTitle")));

        String writingStyles;
        final boolean hasStyles = (book.getWritingStyles() != null)
            && (book.getWritingStyles().size() > 0);

        if (hasStyles)
        {
            StringList styles = new StringList();

            for (Iterator iterator = book.getWritingStyles().iterator();
                iterator.hasNext();)
            {
                Book.WritingStyle style = (Book.WritingStyle) iterator.next();

                styles.add(getLocalizedString(style.toString()));
            }

            writingStyles = styles.toString();
        }
        else
        {
            writingStyles = getLocalizedString("noWritingStyles");
        }

        final Italic italic = new Italic("italicWritingStyles");

        italic.setEnable(!hasStyles);
        add(italic.add(new Label("writingStyles", writingStyles)));
        add(EditBook.link("edit", book.getId()));
    }

    public static void setPage(final RequestCycle cycle, final Book book)
    {
        PageParameters parameters = new PageParameters();

        parameters.put("id", new Long(book.getId()));
        cycle.setPage(BookDetails.class, parameters);
        cycle.setRedirect(true);
    }

    /**
     * Creates an external page link
     * @param name The name of the link component to create
     * @param book The book to link to
     * @param noBookTitle The title to show if book is null
     * @return The external page link
     */
    public static ExternalPageLink link(final String name, final Book book,
        final String noBookTitle)
    {
        final ExternalPageLink link = new ExternalPageLink(name,
                BookDetails.class);

        if (book != null)
        {
            link.setParameter("id", book.getId());
            link.add(new Label("title", book));
        }
        else
        {
            link.add(new Label("title", noBookTitle));
            link.setEnabled(false);
        }

        return link;
    }
}

///////////////////////////////// End of File /////////////////////////////////
