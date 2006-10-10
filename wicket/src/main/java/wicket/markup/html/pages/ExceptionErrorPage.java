/*
 * $Id$
 * $Revision$ $Date$
 * 
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
package wicket.markup.html.pages;

import javax.servlet.http.HttpServletResponse;

import wicket.Page;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.basic.MultiLineLabel;
import wicket.markup.html.debug.PageView;
import wicket.util.string.Strings;

/**
 * Shows a runtime exception on a nice HTML page.
 * 
 * @author Jonathan Locke
 */
public class ExceptionErrorPage extends WebPage
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
	public ExceptionErrorPage(final Throwable throwable, final Page page)
	{
		this.throwable = throwable;
		
		// Add exception label
		add(new MultiLineLabel("exception", Strings.toString(throwable)));

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

		// Create markup label
		final MultiLineLabel markupLabel = new MultiLineLabel("markup", markup);

		markupLabel.setEscapeModelStrings(false);

		// Add container with markup highlighted
		final WebMarkupContainer markupHighlight = new WebMarkupContainer("markupHighlight");

		markupHighlight.add(markupLabel);
		markupHighlight.add(new Label("resource", resource));
		add(markupHighlight);

		// Show container if markup stream is available
		markupHighlight.setVisible(markupStream != null);

		// Show component tree of the page
		if (page != null)
		{
		    add(new PageView("componentTree", page));
		}
		else
		{
		    add(new Label("componentTree", ""));
		}
	}
	
	/**
	 * @see wicket.markup.html.WebPage#configureResponse()
	 */
	protected void configureResponse()
	{
		super.configureResponse();
		getWebRequestCycle().getWebResponse().getHttpServletResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);	
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
