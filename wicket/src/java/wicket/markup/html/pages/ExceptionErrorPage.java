/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.pages;

import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.basic.MultiLineLabel;
import wicket.util.string.Strings;

/**
 * Shows a runtime exception on a nice HTML page.
 * 
 * @author Jonathan Locke
 */
public class ExceptionErrorPage extends WebPage
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 5578704587965089202L;

	/**
	 * Constructor.
	 * 
	 * @param e
	 *            The markup exception to show
	 */
	public ExceptionErrorPage(final RuntimeException e)
	{
		// Add exception label
		add(new MultiLineLabel("exception", Strings.toString(e)));

		// Get values
		String resource = "";
		String markup = "";
		MarkupStream markupStream = null;

		if (e instanceof MarkupException)
		{
			markupStream = ((MarkupException)e).getMarkupStream();

			if (markupStream != null)
			{
				markup = markupStream.toHtmlDebugString();
				resource = markupStream.getResource().toString();
			}
		}

		// Create markup label
		final MultiLineLabel markupLabel = new MultiLineLabel("markup", markup);

		markupLabel.setShouldEscapeModelStrings(false);

		// Add container with markup highlighted
		final WebMarkupContainer markupHighlight = new WebMarkupContainer("markupHighlight");

		markupHighlight.add(markupLabel);
		markupHighlight.add(new Label("resource", resource));
		add(markupHighlight);

		// Show container if markup stream is available
		markupHighlight.setVisible(markupStream != null);
	}
}