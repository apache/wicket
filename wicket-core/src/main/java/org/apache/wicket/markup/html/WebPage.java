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
package org.apache.wicket.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.markup.renderStrategy.AbstractHeaderRenderStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base class for HTML pages. This subclass of Page simply returns HTML when asked for its markup
 * type. It also has a method which subclasses can use to retrieve a bookmarkable link to the
 * application's home page.
 * <p>
 * WebPages can be constructed with any constructor when they are being used in a Wicket session,
 * but if you wish to link to a Page using a URL that is "bookmarkable" (which implies that the URL
 * will not have any session information encoded in it, and that you can call this page directly
 * without having a session first directly from your browser), you need to implement your Page with
 * a no-arg constructor or with a constructor that accepts a PageParameters argument (which wraps
 * any query string parameters for a request). In case the page has both constructors, the
 * constructor with PageParameters will be used.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Gwyn Evans
 */
public class WebPage extends Page
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(WebPage.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor. Having this constructor public means that your page is 'bookmarkable' and hence
	 * can be called/ created from anywhere.
	 */
	protected WebPage()
	{
		commonInit();
	}

	/**
	 * @see Page#Page(IModel)
	 */
	protected WebPage(final IModel<?> model)
	{
		super(model);
		commonInit();
	}

	/**
	 * Constructor which receives wrapped query string parameters for a request. Having this
	 * constructor public means that your page is 'bookmarkable' and hence can be called/ created
	 * from anywhere. For bookmarkable pages (as opposed to when you construct page instances
	 * yourself, this constructor will be used in preference to a no-arg constructor, if both exist.
	 * Note that nothing is done with the page parameters argument. This constructor is provided so
	 * that tools such as IDEs will include it their list of suggested constructors for derived
	 * classes.
	 * 
	 * Please call this constructor if you want to remember the pageparameters
	 * {@link #getPageParameters()}. So that they are reused for stateless links.
	 * 
	 * @param parameters
	 *            Wrapped query string parameters.
	 */
	protected WebPage(final PageParameters parameters)
	{
		super(parameters);
		commonInit();
	}

	/**
	 * Gets the markup type for a WebPage, which is "html" by default. Support for pages in another
	 * markup language, such as VXML, would require the creation of a different Page subclass in an
	 * appropriate package under org.apache.wicket.markup. To support VXML (voice markup), one might
	 * create the package org.apache.wicket.markup.vxml and a subclass of Page called VoicePage.
	 * 
	 * @return Markup type for HTML
	 */
	@Override
	public MarkupType getMarkupType()
	{
		return MarkupType.HTML_MARKUP_TYPE;
	}

	/**
	 * Common code executed by constructors.
	 */
	private void commonInit()
	{
		// so far a noop
	}

	@Override
	protected void onRender()
	{
		// Configure the response such as headers etc.
		configureResponse((WebResponse)RequestCycle.get().getResponse());

		// The rules if and when to insert an xml decl in the response are a it tricky. Allow the
		// user to replace the default per page and per application.
		renderXmlDecl();

		super.onRender();
	}

	/**
	 * The rules if and when to insert an xml decl in the response are a it tricky. Allow the user
	 * to replace the default per page and per application.
	 */
	protected void renderXmlDecl()
	{
		WebApplication.get().renderXmlDecl(this, false);
	}

	/**
	 * Set-up response with appropriate content type, locale and encoding. The locale is set equal
	 * to the session's locale. The content type header contains information about the markup type
	 * (@see #getMarkupType()) and the encoding. The response (and request) encoding is determined
	 * by an application setting (@see ApplicationSettings#getResponseRequestEncoding()). If null,
	 * no xml decl will be written.
	 * 
	 * @param response
	 *            The WebResponse object
	 */
	protected void configureResponse(final WebResponse response)
	{
		// Users may subclass setHeader() to set there own headers
		setHeaders(response);

		// The response encoding is an application setting
		final String encoding = getApplication().getRequestCycleSettings()
			.getResponseRequestEncoding();
		final boolean validEncoding = (Strings.isEmpty(encoding) == false);
		final String contentType;
		if (validEncoding)
		{
			contentType = getMarkupType().getMimeType() + "; charset=" + encoding;
		}
		else
		{
			contentType = getMarkupType().getMimeType();
		}

		response.setContentType(contentType);
	}

	/**
	 * Subclasses can override this to set there headers when the Page is being served. By default
	 * these headers are set:
	 * 
	 * <pre>
	 * response.setHeader(&quot;Date&quot;, &quot;[now]&quot;);
	 * response.setHeader(&quot;Expires&quot;, &quot;[0]&quot;);
	 * response.setHeader(&quot;Pragma&quot;, &quot;no-cache&quot;);
	 * response.setHeader(&quot;Cache-Control&quot;, &quot;no-cache&quot;);
	 * </pre>
	 * 
	 * So if a Page wants to control this or doesn't want to set this info it should override this
	 * method and don't call super.
	 * 
	 * @param response
	 *            The WebResponse where set(Date)Header can be called on.
	 */
	protected void setHeaders(WebResponse response)
	{
		response.disableCaching();
	}

	/**
	 * 
	 * @see org.apache.wicket.Component#onAfterRender()
	 */
	@Override
	protected void onAfterRender()
	{
		super.onAfterRender();

		// only in development mode validate the headers
		if (getApplication().usesDevelopmentConfig())
		{
			// Ignore if an exception and a redirect happened in between (e.g.
			// RestartResponseAtInterceptPageException)
			IRequestHandler activeHandler = getRequestCycle().getActiveRequestHandler();
			if (activeHandler instanceof IPageRequestHandler)
			{
				IPageRequestHandler h = (IPageRequestHandler)activeHandler;
				if (h.getPage() == this)
				{
					validateHeaders();
				}
			}
		}
	}

	/**
	 * Validate that each component which wanted to contribute to the header section actually was
	 * able to do so.
	 */
	private void validateHeaders()
	{
		// search for HtmlHeaderContainer in the first level of children or deeper
		// if there are transparent resolvers used
		HtmlHeaderContainer header = visitChildren(new IVisitor<Component, HtmlHeaderContainer>()
		{
			@Override
			public void component(final Component component, final IVisit<HtmlHeaderContainer> visit)
			{
				if (component instanceof HtmlHeaderContainer)
				{
					visit.stop((HtmlHeaderContainer)component);
				}
				else if (component instanceof TransparentWebMarkupContainer == false)
				{
					visit.dontGoDeeper();
				}
			}
		});

		if (header == null)
		{
			// the markup must at least contain a <body> tag for wicket to automatically
			// create a HtmlHeaderContainer. Log an error if no header container
			// was created but any of the components or behaviors want to contribute
			// something to the header.
			header = new HtmlHeaderContainer(HtmlHeaderSectionHandler.HEADER_ID);
			add(header);

			Response orgResponse = getRequestCycle().getResponse();
			try
			{
				final StringResponse response = new StringResponse();
				getRequestCycle().setResponse(response);

				// Render all header sections of all components on the page
				AbstractHeaderRenderStrategy.get().renderHeader(header, null, getPage());
				response.close();

				if (response.getBuffer().length() > 0)
				{
					// @TODO it is not yet working properly. JDo to fix it
					log.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
					log.error("You probably forgot to add a <body> or <head> tag to your markup since no Header Container was \n" +
						"found but components were found which want to write to the <head> section.\n" +
						response.getBuffer());
					log.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				}
			}
			catch (Exception e)
			{
				// just swallow this exception, there isn't much we can do about.
				log.error("header/body check throws exception", e);
			}
			finally
			{
				this.remove(header);
				getRequestCycle().setResponse(orgResponse);
			}
		}
	}

	/**
	 * Creates and returns a bookmarkable link to this application's home page.
	 * 
	 * @param id
	 *            Name of link
	 * @return Link to home page for this application
	 */
	protected final BookmarkablePageLink<Void> homePageLink(final String id)
	{
		return new BookmarkablePageLink<Void>(id, getApplication().getHomePage());
	}

	/**
	 * Prevents page from get dirt inside an AJAX request.
	 */
	@Override
	public final void dirty(boolean isInitialization)
	{
		Request request = getRequest();
		if (isInitialization == false && request instanceof WebRequest &&
			((WebRequest)request).isAjax())
		{
			return;
		}
		super.dirty(isInitialization);
	}
}
