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
package org.apache.wicket.ajax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnEventHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.internal.HeaderResponse;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.markup.renderStrategy.AbstractHeaderRenderStrategy;
import org.apache.wicket.markup.renderStrategy.IHeaderRenderStrategy;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A POJO-like that collects the data for the Ajax response written to the client
 * and serializes it to specific String-based format (XML, JSON, ...).
 */
public abstract class AbstractAjaxResponse
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractAjaxResponse.class);

	/**
	 * A list of scripts (JavaScript) which should be executed on the client side before the
	 * components' replacement
	 */
	protected final List<CharSequence> prependJavaScripts = Generics.newArrayList();

	/**
	 * A list of scripts (JavaScript) which should be executed on the client side after the
	 * components' replacement
	 */
	protected final List<CharSequence> appendJavaScripts = Generics.newArrayList();

	/**
	 * A list of scripts (JavaScript) which should be executed on the client side after the
	 * components' replacement.
	 * Executed immediately after the replacement of the components, and before appendJavaScripts
	 */
	protected final List<CharSequence> domReadyJavaScripts = Generics.newArrayList();

	/**
	 * The component instances that will be rendered/replaced.
	 */
	protected final Map<String, Component> markupIdToComponent = new LinkedHashMap<String, Component>();

	/**
	 * A flag that indicates that components cannot be added to AjaxRequestTarget anymore.
	 * See https://issues.apache.org/jira/browse/WICKET-3564
	 */
	protected transient boolean componentsFrozen;

	/**
	 * Create a response for component body and javascript that will escape output to make it safe
	 * to use inside a CDATA block
	 */
	protected final AjaxResponse encodingBodyResponse;

	/**
	 * Response for header contribution that will escape output to make it safe to use inside a
	 * CDATA block
	 */
	protected final AjaxResponse encodingHeaderResponse;

	protected HtmlHeaderContainer header = null;

	// whether a header contribution is being rendered
	private boolean headerRendering = false;

	private IHeaderResponse headerResponse;

	/**
	 * The page which components are being updated.
	 */
	private final Page page;

	/**
	 * Constructor.
	 *
	 * @param page
	 *      the page which components are being updated.
	 */
	public AbstractAjaxResponse(final Page page)
	{
		this.page = page;

		Response response = page.getResponse();
		encodingBodyResponse = new AjaxResponse(response);
		encodingHeaderResponse = new AjaxResponse(response);
	}

	/**
	 * Serializes this object to the response.
	 *
	 * @param response
	 *      the response to write to
	 * @param encoding
	 *      the encoding for the response
	 */
	public void writeTo(final Response response, final String encoding)
	{
		writeHeader(response, encoding);

		// invoke onbeforerespond event on listeners
		fireOnBeforeRespondListeners();

		// process added components
		writeComponents(response, encoding);

		fireOnAfterRespondListeners(response);

		// queue up prepend javascripts. unlike other steps these are executed out of order so that
		// components can contribute them from inside their onbeforerender methods.
		writePriorityEvaluations(response, prependJavaScripts);

		// execute the dom ready javascripts as first javascripts
		// after component replacement
		List<CharSequence> evaluationScripts = new ArrayList<CharSequence>();
		evaluationScripts.addAll(domReadyJavaScripts);
		evaluationScripts.addAll(appendJavaScripts);
		writeNormalEvaluations(response, evaluationScripts);

		writeFooter(response, encoding);
	}

	protected abstract void fireOnAfterRespondListeners(Response response);

	protected abstract void fireOnBeforeRespondListeners();

	/**
	 * @param response
	 *      the response to write to
	 * @param encoding
	 *      the encoding for the response
	 */
    protected abstract void writeFooter(Response response, String encoding);

	/**
	 *
	 * @param response
	 *      the response to write to
	 * @param js
	 *      the JavaScript to evaluate
	 */
	protected abstract void writePriorityEvaluations(Response response, Collection<CharSequence> js);

	/**
	 *
	 * @param response
	 *      the response to write to
	 * @param js
	 *      the JavaScript to evaluate
	 */
	protected abstract void writeNormalEvaluations(Response response, Collection<CharSequence> js);

	/**
	 * Processes components added to the target. This involves attaching components, rendering
	 * markup into a client side xml envelope, and detaching them
	 *
	 * @param response
	 *      the response to write to
	 * @param encoding
	 *      the encoding for the response
	 */
	private void writeComponents(Response response, String encoding)
	{
		componentsFrozen = true;

		// process component markup
		for (Map.Entry<String, Component> stringComponentEntry : markupIdToComponent.entrySet())
		{
			final Component component = stringComponentEntry.getValue();

			if (!containsAncestorFor(component))
			{
				writeComponent(response, component.getAjaxRegionMarkupId(), component, encoding);
			}
		}

		if (header != null)
		{
			// some header responses buffer all calls to render*** until close is called.
			// when they are closed, they do something (i.e. aggregate all JS resource urls to a
			// single url), and then "flush" (by writing to the real response) before closing.
			// to support this, we need to allow header contributions to be written in the close
			// tag, which we do here:
			headerRendering = true;
			// save old response, set new
			Response oldResponse = RequestCycle.get().setResponse(encodingHeaderResponse);
			encodingHeaderResponse.reset();

			// now, close the response (which may render things)
			header.getHeaderResponse().close();

			// revert to old response
			RequestCycle.get().setResponse(oldResponse);

			// write the XML tags and we're done
			writeHeaderContribution(response);
			headerRendering = false;
		}
	}

	/**
	 * Writes a single component
	 *
	 * @param response
	 *      the response to write to
	 * @param markupId
	 *      the markup id to use for the component replacement
	 * @param component
	 *      the component which markup will be used as replacement
	 * @param encoding
	 *      the encoding for the response
	 */
	protected abstract void writeComponent(Response response, String markupId, Component component, String encoding);

	/**
	 * Writes the head part of the response.
	 * For example XML preamble
	 *
	 * @param response
	 *      the response to write to
	 * @param encoding
	 *      the encoding for the response
	 */
	protected abstract void writeHeader(Response response, String encoding);

	/**
	 * Writes header contribution (<link/> or <script/>) to the response.
	 *
	 * @param response
	 *      the response to write to
	 */
	protected abstract void writeHeaderContribution(Response response);

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AbstractAjaxResponse that = (AbstractAjaxResponse) o;

		if (!appendJavaScripts.equals(that.appendJavaScripts)) return false;
		if (!domReadyJavaScripts.equals(that.domReadyJavaScripts)) return false;
		return prependJavaScripts.equals(that.prependJavaScripts);
	}

	@Override
	public int hashCode()
	{
		int result = prependJavaScripts.hashCode();
		result = 31 * result + appendJavaScripts.hashCode();
		result = 31 * result + domReadyJavaScripts.hashCode();
		return result;
	}

	/**
	 * Adds script to the ones which are executed after the component replacement.
	 *
	 * @param javascript
	 *      the javascript to execute
	 */
	public final void appendJavaScript(final CharSequence javascript)
	{
		Args.notNull(javascript, "javascript");

		appendJavaScripts.add(javascript);
	}

	/**
	 * Adds script to the ones which are executed before the component replacement.
	 *
	 * @param javascript
	 *      the javascript to execute
	 */
	public final void prependJavaScript(CharSequence javascript)
	{
		Args.notNull(javascript, "javascript");

		prependJavaScripts.add(javascript);
	}

	/**
	 * Adds a component to be updated at the client side with its current markup
	 *
	 * @param component
	 *      the component to update
	 * @param markupId
	 *      the markup id to use to find the component in the page's markup
	 * @throws IllegalArgumentException
	 *      thrown when a Page or an AbstractRepeater is added
	 * @throws IllegalStateException
	 *      thrown when components no more can be added for replacement.
	 */
	public final void add(final Component component, final String markupId)
			throws IllegalArgumentException, IllegalStateException
	{
		Args.notEmpty(markupId, "markupId");
		Args.notNull(component, "component");

		if (component instanceof Page)
		{
			if (component != page)
			{
				throw new IllegalArgumentException("component cannot be a page");
			}
		}
		else if (component instanceof AbstractRepeater)
		{
			throw new IllegalArgumentException(
					"Component " +
							component.getClass().getName() +
							" has been added to the target. This component is a repeater and cannot be repainted via ajax directly. " +
							"Instead add its parent or another markup container higher in the hierarchy.");
		}

		assertComponentsNotFrozen();

		component.setMarkupId(markupId);
		markupIdToComponent.put(markupId, component);
	}

	/**
	 * @return a read-only collection of all components which have been added for replacement so far.
	 */
	public final Collection<? extends Component> getComponents()
	{
		return Collections.unmodifiableCollection(markupIdToComponent.values());
	}

	/**
	 * Detaches the page if at least one of its components was updated.
	 *
	 * @param requestCycle
	 *      the current request cycle
	 */
	public void detach(IRequestCycle requestCycle)
	{
		Iterator<Component> iterator = markupIdToComponent.values().iterator();
		while (iterator.hasNext())
		{
			final Component component = iterator.next();
			final Page parentPage = component.findParent(Page.class);
			if (parentPage != null)
			{
				parentPage.detach();
				break;
			}
		}
	}

	/**
	 * Checks if the target contains an ancestor for the given component
	 *
	 * @param component
	 *      the component which ancestors should be checked.
	 * @return <code>true</code> if target contains an ancestor for the given component
	 */
	protected boolean containsAncestorFor(Component component)
	{
		Component cursor = component.getParent();
		while (cursor != null)
		{
			if (markupIdToComponent.containsValue(cursor))
			{
				return true;
			}
			cursor = cursor.getParent();
		}
		return false;
	}

	/**
	 * @return {@code true} if the page has been added for replacement
	 */
	boolean containsPage()
	{
		return markupIdToComponent.values().contains(page);
	}

	/**
	 * Gets or creates an IHeaderResponse instance to use for the header contributions.
	 *
	 * @return IHeaderResponse instance to use for the header contributions.
	 */
	public IHeaderResponse getHeaderResponse()
	{
		if (headerResponse == null)
		{
			// we don't need to decorate the header response here because this is called from
			// within AjaxHtmlHeaderContainer, which decorates the response
			headerResponse = new AjaxHeaderResponse();
		}
		return headerResponse;
	}

	/**
	 * @param response
	 *      the response to write to
	 * @param component
	 *      to component which will contribute to the header
	 */
	protected void writeHeaderContribution(final Response response, final Component component)
	{
		headerRendering = true;

		// create the htmlheadercontainer if needed
		if (header == null)
		{
			header = new AjaxHtmlHeaderContainer(this);
			final Page parentPage = component.getPage();
			parentPage.addOrReplace(header);
		}

		RequestCycle requestCycle = component.getRequestCycle();

		// save old response, set new
		Response oldResponse = requestCycle.setResponse(encodingHeaderResponse);

		try {
			encodingHeaderResponse.reset();

			IHeaderRenderStrategy strategy = AbstractHeaderRenderStrategy.get();

			strategy.renderHeader(header, null, component);
		} finally {
			// revert to old response
			requestCycle.setResponse(oldResponse);
		}

		writeHeaderContribution(response);

		headerRendering = false;
	}

	/**
	 * Sets the Content-Type header to indicate the type of the Ajax response.
	 *
	 * @param response
	 *      the current we response
	 * @param encoding
	 *      the encoding to use
	 */
	protected abstract void setContentType(WebResponse response, String encoding);


	/**
	 * Header container component for ajax header contributions
	 *
	 * @author Matej Knopp
	 */
	private static class AjaxHtmlHeaderContainer extends HtmlHeaderContainer
	{
		private static final long serialVersionUID = 1L;

		private final transient AbstractAjaxResponse ajaxResponse;

		/**
		 * Constructor.
		 *
		 * @param ajaxResponse
		 *      the object that keeps the data for the Ajax response
		 */
		public AjaxHtmlHeaderContainer(final AbstractAjaxResponse ajaxResponse)
		{
			super(HtmlHeaderSectionHandler.HEADER_ID);
			this.ajaxResponse = ajaxResponse;
		}

		/**
		 *
		 * @see org.apache.wicket.markup.html.internal.HtmlHeaderContainer#newHeaderResponse()
		 */
		@Override
		protected IHeaderResponse newHeaderResponse()
		{
			return ajaxResponse.getHeaderResponse();
		}
	}

	/**
	 * Header response for an ajax request.
	 *
	 * @author Matej Knopp
	 */
	private class AjaxHeaderResponse extends HeaderResponse
	{
		@Override
		public void render(HeaderItem item)
		{
			if (item instanceof OnLoadHeaderItem)
			{
				if (!wasItemRendered(item))
				{
					AbstractAjaxResponse.this.appendJavaScript(((OnLoadHeaderItem) item).getJavaScript());
					markItemRendered(item);
				}
			}
			else if (item instanceof OnEventHeaderItem)
			{
				if (!wasItemRendered(item))
				{
					AbstractAjaxResponse.this.appendJavaScript(((OnEventHeaderItem) item).getCompleteJavaScript());
					markItemRendered(item);
				}
			}
			else if (item instanceof OnDomReadyHeaderItem)
			{
				if (!wasItemRendered(item))
				{
					AbstractAjaxResponse.this.domReadyJavaScripts.add(((OnDomReadyHeaderItem)item).getJavaScript());
					markItemRendered(item);
				}
			}
			else if (headerRendering)
			{
				super.render(item);
			}
			else
			{
				LOG.debug("Only methods that can be called on IHeaderResponse outside renderHead() are #render(OnLoadHeaderItem) and #render(OnDomReadyHeaderItem)");
			}
		}

		@Override
		protected Response getRealResponse()
		{
			return RequestCycle.get().getResponse();
		}
	}

	/**
	 * Response that uses an encoder to encode its contents
	 *
	 * @author Igor Vaynberg (ivaynberg)
	 */
	protected static final class AjaxResponse extends Response
	{
		private final AppendingStringBuffer buffer = new AppendingStringBuffer(256);

		private boolean escaped = false;

		private final Response originalResponse;

		/**
		 * Constructor.
		 *
		 * @param originalResponse
		 *      the original request cycle response
		 */
		private AjaxResponse(Response originalResponse)
		{
			this.originalResponse = originalResponse;
		}

		/**
		 * @see org.apache.wicket.request.Response#encodeURL(CharSequence)
		 */
		@Override
		public String encodeURL(CharSequence url)
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
		 * @return true if any escaping has been performed, false otherwise
		 */
		public boolean isContentsEncoded()
		{
			return escaped;
		}

		/**
		 * @see org.apache.wicket.request.Response#write(CharSequence)
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

		/**
		 * Resets the response to a clean state so it can be reused to save on garbage.
		 */
		@Override
		public void reset()
		{
			buffer.clear();
			escaped = false;
		}

		@Override
		public void write(byte[] array)
		{
			throw new UnsupportedOperationException("Cannot write binary data.");
		}

		@Override
		public void write(byte[] array, int offset, int length)
		{
			throw new UnsupportedOperationException("Cannot write binary data.");
		}

		@Override
		public Object getContainerResponse()
		{
			return originalResponse.getContainerResponse();
		}
	}

	/**
	 * Encodes a string so it is safe to use inside CDATA blocks
	 *
	 * @param str
	 *      the string to encode.
	 * @return encoded string
	 */
	static String encode(CharSequence str)
	{
		if (str == null)
		{
			return null;
		}

		return Strings.replaceAll(str, "]", "]^").toString();
	}

	/**
	 * @return name of encoding used to possibly encode the contents of the CDATA blocks
	 */
	protected String getEncodingName()
	{
		return "wicket1";
	}

	/**
	 *
	 * @param str
	 *      the string to check
	 * @return {@code true} if string needs to be encoded, {@code false} otherwise
	 */
	static boolean needsEncoding(CharSequence str)
	{
		/*
		 * TODO Post 1.2: Ajax: we can improve this by keeping a buffer of at least 3 characters and
		 * checking that buffer so that we can narrow down escaping occurring only for ']]>'
		 * sequence, or at least for ]] if ] is the last char in this buffer.
		 *
		 * but this improvement will only work if we write first and encode later instead of working
		 * on fragments sent to write
		 */
		return Strings.indexOf(str, ']') >= 0;
	}

	private void assertComponentsNotFrozen()
	{
		assertNotFrozen(componentsFrozen, Component.class);
	}

	private void assertNotFrozen(boolean frozen, Class<?> clazz)
	{
		if (frozen)
		{
			throw new IllegalStateException(Classes.simpleName(clazz) + "s can no " +
					" longer be added");
		}
	}
}
