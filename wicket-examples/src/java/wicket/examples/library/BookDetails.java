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
package wicket.examples.library;

import java.util.Iterator;

import wicket.Model;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.ExternalPageLink;
import wicket.util.string.StringList;
import wicket.util.string.StringValueConversionException;


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
                getLocalizer().getString("noBookTitle", this)));
        add(BookDetails.link("related", book.getRelatedBook(),
                getLocalizer().getString("noBookTitle", this)));

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

                styles.add(getLocalizer().getString(style.toString(), this));
            }

            writingStyles = styles.toString();
        }
        else
        {
            writingStyles = getLocalizer().getString("noWritingStyles", this);
        }

        Label writingStylesLabel = new Label("writingStyles", writingStyles);

        final ComponentTagAttributeModifier italic = new ComponentTagAttributeModifier("class", new Model("italic"));
        italic.setEnabled(!hasStyles);

        add(writingStylesLabel.addAttributeModifier(italic));
        add(EditBook.link("edit", book.getId()));
    }

    public static void setPage(final RequestCycle cycle, final Book book)
    {
        PageParameters parameters = new PageParameters();

        parameters.put("id", new Long(book.getId()));
        cycle.setPage(cycle.getPageFactory().newPage(BookDetails.class, parameters));
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
