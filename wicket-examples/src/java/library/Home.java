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

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.table.Cell;
import com.voicetribe.wicket.markup.html.table.Table;

/**
 * Page that displays a list of books and lets the user re-order them.
 * @author Jonathan Locke
 */
public final class Home extends AuthenticatedHtmlPage
{
    /**
     * Constructor
     * @param parameters
      */
    public Home(final PageParameters parameters)
    {
        // Add table of books
        add(new Table("books", getUser(), "books")
            {
                public boolean populateCell(final Cell cell)
                {
                    final Book book = (Book) cell.getModelObject();

                    cell.add(BookDetails.link("details", book,
                            getLocalizer().getString("noBookTitle", this)));
                    cell.add(new Label("author", book));
                    cell.add(cell.moveUpLink("moveUp"));
                    cell.add(cell.moveDownLink("moveDown"));
                    cell.add(cell.removeLink("remove"));
                    cell.add(EditBook.link("edit", book.getId()));
                    
                    return true;
                }
            });
    }
}

///////////////////////////////// End of File /////////////////////////////////
