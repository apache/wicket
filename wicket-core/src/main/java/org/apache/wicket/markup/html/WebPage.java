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

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.markup.renderStrategy.AbstractHeaderRenderStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
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
	private static final Logger log = LoggerFactory.getLogger(WebPage.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 * Having this constructor public means that your page is 'bookmarkable' and hence
	 * can be called/ created from anywhere.
	 */
	protected WebPage()
	{
	}

	protected WebPage(final IModel<?> model)
	{
		super(model);
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

	@Override
	protected void onRender()
	{
		// Configure the response such as headers etc.
		configureResponse((WebResponse)RequestCycle.get().getResponse());

		// The rules if and when to insert an xml decl in the response are a bit tricky. Allow the
		// user to replace the default per page and per application.
		renderXmlDecl();

		super.onRender();
	}

	@Override
	protected boolean renderNext(final MarkupStream markupStream)
	{
		MarkupElement element = markupStream.get();
		if (element instanceof ComponentTag tag && tag.isOpen() &&
			tag.getFlag(ComponentTag.RENDERED_BY_PAGE))
		{
			ComponentTag htmlTag = tag.mutable();
			onHtmlTag(htmlTag);
			htmlTag.writeOutput(getResponse(), getApplication().getMarkupSettings()
				.getStripWicketTags(), getMarkup().getMarkupResourceStream().getWicketNamespace());
			return true;
		}
		return super.renderNext(markupStream);
	}

	/**
	 * Called when the &lt;html&gt; tag of this page is rendered, to let the page add or change its
	 * attributes. The default sets <code>lang</code> to the language tag of {@link #getLocale()},
	 * for example <code>nl-NL</code>, so that screen readers and browsers can determine the
	 * language of the page (WCAG 2.2 success criterion 3.1.1), and <code>dir="rtl"</code> when
	 * that language is written right to left, see {@link Session#isRtlLanguage(Locale)}. It does
	 * not set <code>dir="ltr"</code>, which is the default of the browser.
	 * <p>
	 * It leaves the tag alone when the markup already has a <code>lang</code> or
	 * <code>xml:lang</code> attribute, so a page written in one language whatever the session
	 * locale can say so in its markup, and when the locale has no language, such as
	 * {@link Locale#ROOT}. A <code>dir</code> in the markup is kept as well: <code>dir="ltr"</code>
	 * keeps a page left to right in every locale.
	 * <p>
	 * Override this method to add attributes that belong to the document as a whole, and call
	 * <code>super</code> to keep the language and direction. An application whose stylesheets do
	 * not support right-to-left layout can opt out of the direction in its base page:
	 *
	 * <pre>
	 * &#64;Override
	 * protected void onHtmlTag(ComponentTag tag)
	 * {
	 * 	super.onHtmlTag(tag);
	 * 	tag.remove("dir");
	 * }
	 * </pre>
	 *
	 * Attribute values are escaped when the tag is written.
	 * <p>
	 * With markup inheritance the &lt;html&gt; tag is the one of the base page; markup that
	 * chooses between several &lt;html&gt; tags with conditional comments has this method called
	 * for each of them. An &lt;html&gt; tag with a <code>wicket:id</code> belongs to its component
	 * and is not passed here. The tag is written from its attributes, the way a component tag is.
	 * <p>
	 * The &lt;html&gt; tag is only rendered with the whole page. An Ajax request does not render it
	 * again, so an attribute that has to change in response to an Ajax request, such as a
	 * <code>data-theme</code> switched by an <code>AjaxLink</code>, has to be changed in the
	 * browser as well, for instance with
	 * <code>target.appendJavaScript("document.documentElement.dataset.theme='dark'")</code>.
	 *
	 * @param tag
	 *            a mutable copy of the &lt;html&gt; tag of the page's markup
	 * @since 12.0.0
	 */
	protected void onHtmlTag(final ComponentTag tag)
	{
		if (tag.getAttribute("lang") != null || tag.getAttribute("xml:lang") != null)
		{
			return;
		}

		Locale locale = getLocale();
		if (locale == null || locale.getLanguage().isEmpty())
		{
			return;
		}

		tag.put("lang", locale.toLanguageTag());
		if (tag.getAttribute("dir") == null && Session.isRtlLanguage(locale))
		{
			tag.put("dir", "rtl");
		}
	}

	/**
	 * The rules if and when to insert an xml decl in the response are a bit tricky. Allow the user
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
		var cspSettings = WebApplication.get().getCspSettings();
		if (cspSettings.mustProtect(this) && response.isHeaderSupported())
		{
			cspSettings.getHeaderWriter().write(response, getRequestCycle());
		}

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
		// only in development mode validate the headers
		if (getApplication().usesDevelopmentConfig())
		{
			// check headers only when page was completely rendered
			if (wasRendered(this))
			{
				validateHeaders();
			}
		}

		super.onAfterRender();
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

			RequestCycle requestCycle = getRequestCycle();
			Response orgResponse = requestCycle.getResponse();
			try
			{
				StringResponse tempResponse = new StringResponse();
				requestCycle.setResponse(tempResponse);

				// Render all header sections of all components on the page
				AbstractHeaderRenderStrategy.get().renderHeader(header, null, getPage());

				IHeaderResponse headerResponse = header.getHeaderResponse();
				headerResponse.close();
				CharSequence collectedHeaderOutput = tempResponse.getBuffer();
				if (collectedHeaderOutput.length() > 0)
				{
					reportMissingHead(collectedHeaderOutput);
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
				requestCycle.setResponse(orgResponse);
			}
		}
	}

	/**
	 * Reports an error that there is no &lt;head&gt; and/or &lt;body&gt; in the page and
	 * there is no where to write the header response.
	 * 
	 * @param collectedHeaderOutput
	 *          The collected response that should have been written to the &lt;head&gt;
	 * @see #validateHeaders()
	 */
	protected void reportMissingHead(final CharSequence collectedHeaderOutput)
	{
		log.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		log.error("You probably forgot to add a <body> or <head> tag to your markup since no Header Container was \n" +
				"found but components were found which want to write to the <head> section.\n" +
				collectedHeaderOutput);
		log.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
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
		return new BookmarkablePageLink<>(id, getApplication().getHomePage());
	}

	/**
	 * Prevents page from get dirty inside an AJAX request.
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
