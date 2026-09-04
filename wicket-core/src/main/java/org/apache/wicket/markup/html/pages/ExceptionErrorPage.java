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

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.debug.PageView;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Generics;


/**
 * Shows a runtime exception on a nice HTML page.
 * 
 * @author Jonathan Locke
 */
public class ExceptionErrorPage extends AbstractErrorPage
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
		add(new MultiLineLabel("exception", getErrorMessage(throwable)));

		add(new MultiLineLabel("stacktrace", getStackTrace(throwable)));

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

		add(new Link<Void>("displayPageViewLink")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				ExceptionErrorPage.this.replace(new PageView("componentTree", page));
				setVisible(false);
			}
		});

		add(new Label("componentTree", ""));
	}

	/**
	 * Converts a Throwable to a string.
	 * 
	 * @param throwable
	 *            The throwable
	 * @return The string
	 */
	public String getErrorMessage(final Throwable throwable)
	{
		if (throwable != null)
		{
			StringBuilder sb = new StringBuilder(256);

			// first print the last cause
			List<Throwable> al = convertToList(throwable);
			int length = al.size() - 1;
			Throwable cause = al.get(length);
			sb.append("Last cause: ").append(cause.getMessage()).append('\n');
			if (throwable instanceof WicketRuntimeException)
			{
				String msg = throwable.getMessage();
				if ((msg != null) && (msg.equals(cause.getMessage()) == false))
				{
					if (throwable instanceof MarkupException)
					{
						MarkupStream stream = ((MarkupException)throwable).getMarkupStream();
						if (stream != null)
						{
							String text = "\n" + stream.toString();
							if (msg.endsWith(text))
							{
								msg = msg.substring(0, msg.length() - text.length());
							}
						}
					}

					sb.append("WicketMessage: ");
					sb.append(msg);
					sb.append("\n\n");
				}
			}
			return sb.toString();
		}
		else
		{
			return "[Unknown]";
		}
	}

	/**
	 * Converts a Throwable to a string.
	 * 
	 * @param throwable
	 *            The throwable
	 * @return The string
	 */
	public String getStackTrace(final Throwable throwable)
	{
		if (throwable != null)
		{
			List<Throwable> al = convertToList(throwable);

			StringBuilder sb = new StringBuilder(256);

			// first print the last cause
			int length = al.size() - 1;
			Throwable cause = al.get(length);

			sb.append("Root cause:\n\n");
			outputThrowable(cause, sb, false);

			if (length > 0)
			{
				sb.append("\n\nComplete stack:\n\n");
				for (int i = 0; i < length; i++)
				{
					outputThrowable(al.get(i), sb, true);
					sb.append("\n");
				}
			}
			return sb.toString();
		}
		else
		{
			return "<Null Throwable>";
		}
	}

	/**
	 * @param throwable
	 * @return xxx
	 */
	private List<Throwable> convertToList(final Throwable throwable)
	{
		List<Throwable> al = Generics.newArrayList();
		Throwable cause = throwable;
		al.add(cause);
		while ((cause.getCause() != null) && (cause != cause.getCause()))
		{
			cause = cause.getCause();
			al.add(cause);
		}
		return al;
	}

	/**
	 * Outputs the throwable and its stacktrace to the stringbuffer. If stopAtWicketSerlvet is true
	 * then the output will stop when the org.apache.wicket servlet is reached. sun.reflect.
	 * packages are filtered out.
	 * 
	 * @param cause
	 * @param sb
	 * @param stopAtWicketServlet
	 */
	private void outputThrowable(Throwable cause, StringBuilder sb, boolean stopAtWicketServlet)
	{
		sb.append(cause);
		sb.append("\n");
		StackTraceElement[] trace = cause.getStackTrace();
		for (int i = 0; i < trace.length; i++)
		{
			String traceString = trace[i].toString();
			if (!(traceString.startsWith("sun.reflect.") && i > 1))
			{
				sb.append("     at ");
				sb.append(traceString);
				sb.append("\n");
				if (stopAtWicketServlet &&
					(traceString.startsWith("org.apache.wicket.protocol.http.WicketServlet") || traceString.startsWith("org.apache.wicket.protocol.http.WicketFilter")))
				{
					return;
				}
			}
		}
	}

	@Override
	protected void setHeaders(final WebResponse response)
	{
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
}
