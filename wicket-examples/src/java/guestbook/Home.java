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
package guestbook;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.basic.MultiLineLabel;
import com.voicetribe.wicket.markup.html.form.Form;
import com.voicetribe.wicket.markup.html.form.TextArea;
import com.voicetribe.wicket.markup.html.table.Cell;
import com.voicetribe.wicket.markup.html.table.Table;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple "guest book" example that allows visitors to the page to add a comment
 * and see the comments others have added.
 * @author Jonathan Locke
 */
public class Home extends HtmlPage
{
    // A global list of all comments from all users
    private static final List commentList = new ArrayList();

    // The table of comments shown on this page
    private final Table table;

    /**
     * Constructor that is invoked when page is invoked without a session.
     * @param parameters Page parameters
     */
    public Home(final PageParameters parameters)
    {
        // Add comment form
        add(new CommentForm("commentForm"));

        // Add table of existing comments
        add(table = new Table("comments", commentList)
                {
                    public boolean populateCell(final Cell cell)
                    {
                        final Comment comment = (Comment) cell.getModelObject();

                        cell.add(new Label("date", comment.getDate()));
                        cell.add(new MultiLineLabel("text", comment.getText()));
                        return true;
                    }
                });
    }

    /**
     * A form that allows a user to add a comment.
     * @author Jonathan Locke
     */
    public final class CommentForm extends Form
    {
        // The comment that this form is editing
        private final Comment comment = new Comment();

        /**
         * Constructor
         * @param componentName The name of this component
         */
        public CommentForm(final String componentName)
        {
            // Construct form with no validation listener
            super(componentName, null);

            // Add text entry widget
            add(new TextArea("text", comment, "text"));
        }

        /**
         * Show the resulting valid edit
         * @param cycle The request cycle
         */
        public final void handleSubmit(final RequestCycle cycle)
        {
            // Construct a copy of the edited comment
            final Comment newComment = new Comment(comment);

            // Set date of comment to add
            newComment.setDate(new Date());

            // Add the component we edited to the list of comments
            commentList.add(newComment);

            // Invalidate the table's model since a structural change
            // was made to the comment list model
            table.invalidateModel();

            // Clear out the text component
            comment.setText("");
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
