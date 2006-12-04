/*
 * $Id$ $Revision$ $Date$
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
package wicket.examples.compref;

import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.link.BookmarkablePageLink;

/**
 * Page with examples on {@link wicket.markup.html.link.BookmarkablePageLink}.
 * 
 * @author Eelco Hillenius
 */
public class BookmarkablePageLinkPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public BookmarkablePageLinkPage()
	{
		// Addbookmarkable page links. A page is bookmarkable when it has a
		// public default
		// constructor and/ or a constructor with a PageParameters argument

		// Here, we add a link to a bookmarkable page without passing any
		// parameters
		add(new BookmarkablePageLink("pageLinkNoArgs", BookmarkablePage.class));

		// And here, we add a link to a bookmarkable page with passing a
		// parameter that holds
		// the message that is to be displayed in the page we address.
		// Note that any arguments are passed as request parameters, and should
		// thus be strings
		PageParameters parameters = new PageParameters();
		parameters.put("message", "This message was passed as a page parameter argument");
		add(new BookmarkablePageLink("pageLinkWithArgs", BookmarkablePage.class, parameters));
	}

	/**
	 * Override base method to provide an explanation
	 */
	protected void explain()
	{
		String html = "<a wicket:id=\"pageLinkWithArgs\">go to our bookmarkable page passing a message argument</a>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// Note that any arguments are passed as request parameters, and should thus be strings\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;PageParameters parameters = new PageParameters();\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;parameters.put(\"message\", \"This message was passed as a page parameter argument\");\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add(new BookmarkablePageLink(\"pageLinkWithArgs\", BookmarkablePage.class, parameters));";
		add(new ExplainPanel(html, code));

	}

}