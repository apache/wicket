/*
 * $Id: AjaxRequestTarget.java 4837 2006-03-08 14:46:58 -0800 (Wed, 08 Mar 2006)
 * ivaynberg $ $Revision$ $Date: 2006-03-08 14:46:58 -0800 (Wed, 08 Mar
 * 2006) $
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
package wicket.ajax;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.IRequestTarget;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.RequestCycle;
import wicket.Response;
import wicket.Session;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.protocol.http.WebResponse;
import wicket.request.target.IRequestTargetInterceptor;
import wicket.request.target.component.IPageRequestTarget;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * A request target that produces ajax response envelopes used on the client
 * side to update component markup as well as evaluate arbitrary javascript.
 * <p>
 * A component whose markup needs to be updated should be added to this target
 * via AjaxRequestTarget#addComponent(Component) method. Its body will be
 * rendered and added to the envelope when the target is processed, and
 * refreshed on the client side when the ajax response is received.
 * <p>
 * It is important that the component whose markup needs to be updated contains
 * an id attribute in the generated markup that is equal to the value retrieved
 * from Component#getMarkupId(). This can be accomplished by either setting the
 * id attribute in the html template, or using an attribute modifier that will
 * add the attribute with value Component#getMarkupId() to the tag ( such as
 * MarkupIdSetter )
 * <p>
 * Any javascript that needs to be evaluater on the client side can be added
 * using AjaxRequestTarget#addJavascript(String). For example, this feature can
 * be useful when it is desirable to link component update with some javascript
 * effects.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class AjaxRequestTarget implements IRequestTarget, IRequestTargetInterceptor
{
	/**
	 * Response that uses an encoder to encode its contents
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	private final class EncodingResponse extends WebResponse
	{
		private final AppendingStringBuffer buffer = new AppendingStringBuffer(256);

		private boolean escaped = false;

		private final Response originalResponse;

		/**
		 * Construct.
		 * 
		 * @param originalResponse
		 */
		public EncodingResponse(Response originalResponse)
		{
			this.originalResponse = originalResponse;
		}

		/**
		 * @see wicket.Response#encodeURL(CharSequence)
		 */
		@Override
		public CharSequence encodeURL(CharSequence url)
		{
			return originalResponse.encodeURL(url);
		}

		/**
		 * @return contents of the response
		 */
		public CharSequence getContents()
		{
			return buffer;
		}

		/**
		 * NOTE: this method is not supported
		 * 
		 * @see wicket.Response#getOutputStream()
		 */
		@Override
		public OutputStream getOutputStream()
		{
			throw new UnsupportedOperationException("Cannot get output stream on StringResponse");
		}

		/**
		 * @return true if any escaping has been performed, false otherwise
		 */
		public boolean isContentsEncoded()
		{
			return escaped;
		}

		/**
		 * Resets the response to a clean state so it can be reused to save on
		 * garbage.
		 */
		public void reset()
		{
			buffer.clear();
			escaped = false;

		}

		/**
		 * @see wicket.Response#write(CharSequence)
		 */
		@Override
		public void write(CharSequence cs)
		{
			String string = cs.toString();
			if (needsEncoding(string))
			{
				string = encode(string);
				escaped = true;
				buffer.append(string);
			}
			else
			{
				buffer.append(cs);
			}
		}

	}

	private static final Log LOG = LogFactory.getLog(AjaxRequestTarget.class);

	private final List<String> appendJavascripts = new ArrayList<String>();

	/**
	 * create a response that will escape output to make it safe to use inside a
	 * CDATA block
	 */
	private final EncodingResponse encodingResponse;

	/** the component instances that will be rendered */
	private final Map<String, Component> markupIdToComponent = new HashMap<String, Component>();

	private final List<String> prependJavascripts = new ArrayList<String>();

	/**
	 * Any request target to redirect to. if not null, overrides any other
	 * response.
	 */
	private IRequestTarget requestTarget;

	/**
	 * Constructor
	 */
	public AjaxRequestTarget()
	{
		Response response = RequestCycle.get().getResponse();
		encodingResponse = new EncodingResponse(response);
	}


	/**
	 * Adds a component to the list of components to be rendered
	 * 
	 * @param component
	 *            component to be rendered
	 */
	public final void addComponent(Component component)
	{
		addComponent(component, component.getMarkupId());
	}

	/**
	 * Adds a component to the list of components to be rendered
	 * 
	 * @param markupId
	 *            id of client-side dom element that will be updated
	 * 
	 * @param component
	 *            component to be rendered
	 */
	public final void addComponent(Component component, String markupId)
	{
		if (Strings.isEmpty(markupId))
		{
			throw new IllegalArgumentException("markupId cannot be empty");
		}
		if (component == null)
		{
			throw new IllegalArgumentException("component cannot be null");
		}
		else if (component instanceof Page)
		{
			throw new IllegalArgumentException("component cannot be a page");
		}

		markupIdToComponent.put(markupId, component);
	}

	/**
	 * Adds javascript that will be evaluated on the client side after
	 * components are replaced
	 * 
	 * @deprecated use appendJavascript(String javascript) instead
	 * @param javascript
	 */
	public final void addJavascript(String javascript)
	{
		appendJavascript(javascript);
	}

	/**
	 * Adds javascript that will be evaluated on the client side after
	 * components are replaced
	 * 
	 * @param javascript
	 */
	public final void appendJavascript(String javascript)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("javascript cannot be null");
		}

		appendJavascripts.add(javascript);
	}

	/**
	 * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
	 */
	public void detach(final RequestCycle requestCycle)
	{
	}


	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof AjaxRequestTarget)
		{
			AjaxRequestTarget that = (AjaxRequestTarget)obj;
			return markupIdToComponent.equals(that.markupIdToComponent)
					&& prependJavascripts.equals(that.prependJavascripts)
					&& appendJavascripts.equals(that.appendJavascripts);
		}
		return false;
	}


	/**
	 * @see wicket.IRequestTarget#getLock(RequestCycle)
	 */
	public Object getLock(final RequestCycle requestCycle)
	{
		return requestCycle.getSession();
	}

	/**
	 * Gets any request target to redirect to. if not null, overrides any other
	 * response. <strong>This method is not meant for to be used by framework
	 * clients.</strong>
	 * 
	 * @return requestTarget any request target
	 */
	public final IRequestTarget getRequestTarget()
	{
		return requestTarget;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = "AjaxRequestTarget".hashCode();
		result += markupIdToComponent.hashCode() * 17;
		result += prependJavascripts.hashCode() * 17;
		result += appendJavascripts.hashCode() * 17;
		return result;
	}

	/**
	 * Sets Any request target to redirect to. if not null, overrides any other
	 * response. <strong>This method is not meant for to be used by framework
	 * clients.</strong>
	 * 
	 * @param requestTarget
	 *            requestTarget the request target
	 * @return Null as it always 'eats' the request to set another request
	 *         target as the current one at {@link RequestCycle}
	 * @see wicket.request.target.IRequestTargetInterceptor#onSetRequestTarget(wicket.IRequestTarget)
	 */
	public IRequestTarget onSetRequestTarget(IRequestTarget requestTarget)
	{
		this.requestTarget = requestTarget;
		return null;
	}

	/**
	 * Adds javascript that will be evaluated on the client side before
	 * components are replaced
	 * 
	 * @param javascript
	 */
	public final void prependJavascript(String javascript)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("javascript cannot be null");
		}

		prependJavascripts.add(javascript);
	}

	/**
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public final void respond(final RequestCycle requestCycle)
	{
		try
		{
			CharSequence url = null;

			if (requestTarget != null)
			{
				// a request target was set. Try to get the url for a redirect
				// to that
				url = requestCycle.urlFor(requestTarget);
				// there was a requestTarget, but couldn't generate a redirect
				// url.
				// then just call respond to it. It should be a request target
				// that handles
				// the complete output by itself.
				if (url == null)
				{
					requestTarget.respond(requestCycle);
					return;
				}
			}


			final Application app = Application.get();

			// disable component use check since we want to ignore header
			// contribs
			final boolean oldUseCheck = app.getDebugSettings().getComponentUseCheck();
			app.getDebugSettings().setComponentUseCheck(false);

			// Determine encoding
			final String encoding = app.getRequestCycleSettings().getResponseRequestEncoding();

			// Set content type based on markup type for page
			WebResponse response = (WebResponse)requestCycle.getResponse();
			response.setCharacterEncoding(encoding);
			response.setContentType("text/xml; charset=" + encoding);

			// Make sure it is not cached by a
			response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
			response.setHeader("Cache-Control", "no-cache, must-revalidate");
			response.setHeader("Pragma", "no-cache");

			response.write("<?xml version=\"1.0\" encoding=\"");
			response.write(encoding);
			response.write("\"?>");
			response.write("<ajax-response>");

			if (url == null)
			{
				// normal behavior

				for (String js : prependJavascripts)
				{
					respondInvocation(response, js);
				}

				Iterator<Entry<String, Component>> it = markupIdToComponent.entrySet().iterator();
				while (it.hasNext())
				{
					final Map.Entry<String, Component> entry = it.next();
					final Component component = entry.getValue();
					final String markupId = entry.getKey();
					respondHeaderContribution(response, component);
					respondComponent(response, markupId, component);
				}

				for (String js : appendJavascripts)
				{
					respondInvocation(response, js);
				}
			}
			else
			{
				// if this is a page target, make sure it is available in the
				// page map
				if (requestTarget instanceof IPageRequestTarget)
				{
					Session.get().touch(((IPageRequestTarget)requestTarget).getPage());
				}

				// append the redirect script
				respondInvocation(response, "window.location='" + url + "';");
			}

			response.write("</ajax-response>");

			// restore component use check
			app.getDebugSettings().setComponentUseCheck(oldUseCheck);
		}
		catch (RuntimeException ex)
		{
			// log the error but output nothing in the response, parse failure
			// of response will cause any javascript failureHandler to be
			// invoked
			LOG.error("Error while responding to an AJAX request: " + toString(), ex);
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[AjaxRequestTarget@" + hashCode() + " markupIdToComponent [" + markupIdToComponent
				+ "], prependJavascript [" + prependJavascripts + "], appendJavascript ["
				+ appendJavascripts + "]";
	}

	/**
	 * Encodes a string so it is safe to use inside CDATA blocks
	 * 
	 * @param str
	 * @return encoded string
	 */
	protected String encode(String str)
	{
		// TODO Post 1.2: Java5: we can use str.replace(charseq, charseq) for
		// more efficient replacement
		return str.replaceAll("]", "]^");
	}

	/**
	 * @return name of encoding used to possibly encode the contents of the
	 *         CDATA blocks
	 */
	protected String getEncodingName()
	{
		return "wicket1";
	}

	/**
	 * 
	 * @param str
	 * @return true if string needs to be encoded, false otherwise
	 */
	protected boolean needsEncoding(String str)
	{
		/*
		 * TODO Post 1.2: Ajax: we can improve this by keeping a buffer of at
		 * least 3 characters and checking that buffer so that we can narrow
		 * down escaping occuring only for ']]>' sequence, or at least for ]] if ]
		 * is the last char in this buffer.
		 * 
		 * but this improvement will only work if we write first and encode
		 * later instead of working on fragments sent to write
		 */

		return str.indexOf(']') >= 0;
	}

	/**
	 * 
	 * @param response
	 * @param markupId
	 *            id of client-side dom element
	 * @param component
	 *            component to render
	 */
	private void respondComponent(final Response response, final String markupId,
			final Component component)
	{
		if (component.getRenderBodyOnly() == true)
		{
			throw new IllegalStateException(
					"Ajax render cannot be called on component that has setRenderBodyOnly enabled. Component: "
							+ component.toString());
		}

		component.setOutputMarkupId(true);

		// substitute our encoding response for the real one so we can capture
		// component's markup in a manner safe for transport inside CDATA block
		final Response originalResponse = response;
		encodingResponse.reset();
		RequestCycle.get().setResponse(encodingResponse);

		// Initialize temporary variables
		final Page page = component.getPage();
		if (page == null)
		{
			throw new IllegalStateException(
					"Ajax request attempted on a component that is not associated with a Page");
		}

		final boolean versioned = page.isVersioned();
		page.setVersioned(false);

		page.startComponentRender(component);
		component.renderComponent();
		page.endComponentRender(component);

		page.setVersioned(versioned);

		// Restore original response
		RequestCycle.get().setResponse(originalResponse);

		response.write("<component id=\"");
		response.write(markupId);
		response.write("\" ");
		if (encodingResponse.isContentsEncoded())
		{
			response.write(" encoding=\"");
			response.write(getEncodingName());
			response.write("\" ");
		}
		response.write("><![CDATA[");
		response.write(encodingResponse.getContents());
		response.write("]]></component>");

		encodingResponse.reset();


	}

	private HtmlHeaderContainer header = null;

	/**
	 * 
	 * @param response
	 * @param component
	 */
	private void respondHeaderContribution(final Response response, final Component component)
	{
		if (header == null)
		{
			header = new HtmlHeaderContainer(component.getPage(),HtmlHeaderSectionHandler.HEADER_ID);
		}

		Response oldResponse = RequestCycle.get().setResponse(encodingResponse);

		encodingResponse.reset();

		component.renderHead(header);
		if (component instanceof MarkupContainer)
		{
			((MarkupContainer)component).visitChildren(new Component.IVisitor()
			{
				public Object component(Component component)
				{
					if (component.isVisible())
					{
						component.renderHead(header);
						return CONTINUE_TRAVERSAL;
					}
					else
					{
						return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
					}
				}
			});
		}

		RequestCycle.get().setResponse(oldResponse);

		if (encodingResponse.getContents().length() != 0)
		{
			response.write("<header-contribution");

			if (encodingResponse.isContentsEncoded())
			{
				response.write(" encoding=\"");
				response.write(getEncodingName());
				response.write("\" ");
			}

			// we need to write response as CDATA and parse it on client,
			// because
			// konqueror crashes when there is a <script> element
			response.write("><![CDATA[<head xmlns:wicket=\"http://wicket.sourceforge.net\">");

			response.write(encodingResponse.getContents());

			response.write("</head>]]>");

			response.write("</header-contribution>");
		}
	}

	/**
	 * 
	 * @param response
	 * @param js
	 */
	private void respondInvocation(final Response response, final String js)
	{
		boolean encoded = false;
		String javascript = js;

		// encode the response if needed
		if (needsEncoding(js))
		{
			encoded = true;
			javascript = encode(js);
		}

		response.write("<evaluate");
		if (encoded)
		{
			response.write(" encoding=\"");
			response.write(getEncodingName());
			response.write("\"");
		}
		response.write(">");
		response.write("<![CDATA[");
		response.write(javascript);
		response.write("]]>");
		response.write("</evaluate>");

		encodingResponse.reset();
	}
}