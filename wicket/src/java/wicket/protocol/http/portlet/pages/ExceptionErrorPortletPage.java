/*
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
package wicket.protocol.http.portlet.pages;

import wicket.Page;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.basic.MultiLineLabel;
import wicket.markup.html.debug.PageView;
import wicket.protocol.http.portlet.PortletPage;
import wicket.util.string.Strings;

/**
 * Exception error portlet page
 * 
 * @author Janne Hietam&auml;ki
 */
public class ExceptionErrorPortletPage extends PortletPage
{	
	private static final long serialVersionUID = 1L;

	/** Keep a reference to the root cause. WicketTester will use it */
	private transient Throwable throwable;

	/**
	 * Constructor.
	 * 
	 * @param throwable
	 *            The exception to show
	 * @param page
	 *            The page being rendered when the exception was thrown
	 */
	public ExceptionErrorPortletPage(final Throwable throwable, final Page page)
	{
		this.throwable = throwable;

		// Add exception label
		new MultiLineLabel(this, "exception", Strings.toString(throwable));

		// Get values
		String resource = "";
		String markup = "";
		MarkupStream markupStream = null;

		if (throwable instanceof MarkupException)
		{
			markupStream = ((MarkupException)throwable).getMarkupStream();

			if (markupStream != null)
			{
				markup = markupStream.toHtmlDebugString();
				resource = markupStream.getResource().toString();
			}
		}

		// Add container with markup highlighted
		final WebMarkupContainer markupHighlight = new WebMarkupContainer(this, "markupHighlight");
		// Create markup label
		final MultiLineLabel markupLabel = new MultiLineLabel(markupHighlight, "markup", markup);

		markupLabel.setEscapeModelStrings(false);

		new Label(markupHighlight, "resource", resource);


		// Show container if markup stream is available
		markupHighlight.setVisible(markupStream != null);

		// Show component tree of the page
		if (page != null)
		{
			new PageView(this, "componentTree", page);
		}
		else
		{
			new Label(this, "componentTree", "");
		}
	}

	/**
	 * @see wicket.markup.html.WebPage#configureResponse()
	 */
	protected void configureResponse()
	{
		super.configureResponse();
		//getWebRequestCycle().getWebResponse().getHttpServletResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);	
	}

	/**
	 * Get access to the exception
	 * 
	 * @return The exception
	 */
	public Throwable getThrowable() 
	{
		return throwable;
	}

	/**
	 * @see wicket.Page#isErrorPage()
	 */
	public boolean isErrorPage()
	{
		return true;
	}

	/**
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}
}
