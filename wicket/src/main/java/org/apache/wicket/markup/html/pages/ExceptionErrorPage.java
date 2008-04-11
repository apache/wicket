/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.pages;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Page;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.debug.PageView;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.string.Strings;


/**
 * Shows a runtime exception on a nice HTML page.
 * 
 * @author Jonathan Locke
 */
public class ExceptionErrorPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/** Keep a reference to the root cause. WicketTester will use it */
	private final transient Throwable throwable;

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

		add(new Link("displayPageViewLink")
		{
			private static final long serialVersionUID = 1L;

			public void onClick()
			{
				ExceptionErrorPage.this.replace(new PageView("componentTree", page));
				setVisible(false);
			}
		});

		add(new Label("componentTree", ""));
	}

	/**
	 * @see org.apache.wicket.markup.html.WebPage#configureResponse()
	 */
	protected void configureResponse()
	{
		super.configureResponse();

		if (getWebRequestCycle().getResponse() instanceof WebResponse)
		{
			getWebRequestCycle().getWebResponse().getHttpServletResponse().setStatus(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
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
	 * @see org.apache.wicket.Page#isErrorPage()
	 */
	public boolean isErrorPage()
	{
		return true;
	}

	/**
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}
}
