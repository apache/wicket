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
package org.apache.wicket.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.IWrappedHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnEventHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
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
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A partial update of a page that collects components and header contributions to be written to the client in a specific
 * String-based format (XML, JSON, * ...).
 * <p>
 * The elements of such response are:
 * <ul>
 * <li>priority-evaluate - an item of the prepend JavaScripts</li>
 * <li>component - the markup of the updated component</li>
 * <li>evaluate - an item of the onDomReady and append JavaScripts</li>
 * <li>header-contribution - all HeaderItems which have been contributed in
 * components' and their behaviors' #renderHead(Component, IHeaderResponse)</li>
 * </ul>
 */
public abstract class PartialPageUpdate
{
	private static final Logger LOG = LoggerFactory.getLogger(PartialPageUpdate.class);

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
	 * A flag that indicates that components cannot be added anymore.
	 * See https://issues.apache.org/jira/browse/WICKET-3564
	 * 
	 * @see #add(Component, String)
	 */
	protected transient boolean componentsFrozen;

	/**
	 * Buffer of response body. 
	 */
	protected final ResponseBuffer bodyBuffer;

	/**
	 * Buffer of response header.
	 */
	protected final ResponseBuffer headerBuffer;

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
	public PartialPageUpdate(final Page page)
	{
		this.page = page;

		WebResponse response = (WebResponse) page.getResponse();
		bodyBuffer = new ResponseBuffer(response);
		headerBuffer = new ResponseBuffer(response);
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

		onBeforeRespond(response);

		// process added components
		writeComponents(response, encoding);

		onAfterRespond(response);

		// queue up prepend javascripts. unlike other steps these are executed out of order so that
		// components can contribute them from inside their onbeforerender methods.
		writePriorityEvaluations(response, prependJavaScripts);

		// execute the dom ready javascripts as first javascripts
		// after component replacement
		List<CharSequence> evaluationScripts = new ArrayList<>();
		evaluationScripts.addAll(domReadyJavaScripts);
		evaluationScripts.addAll(appendJavaScripts);
		writeNormalEvaluations(response, evaluationScripts);

		writeFooter(response, encoding);
	}

	protected void onBeforeRespond(Response response) {
	}

	protected void onAfterRespond(Response response) {
	}

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
			Response oldResponse = RequestCycle.get().setResponse(headerBuffer);
			headerBuffer.reset();

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

		PartialPageUpdate that = (PartialPageUpdate) o;

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
							" has been added to a partial page update. This component is a repeater and cannot be repainted directly. " +
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
	public boolean containsPage()
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
			// within PartialHtmlHeaderContainer, which decorates the response
			headerResponse = new PartialHeaderResponse();
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
			header = new PartialHtmlHeaderContainer(this);
			final Page parentPage = component.getPage();
			parentPage.addOrReplace(header);
		}

		RequestCycle requestCycle = component.getRequestCycle();

		// save old response, set new
		Response oldResponse = requestCycle.setResponse(headerBuffer);

		try {
			headerBuffer.reset();

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
	 * Sets the Content-Type header to indicate the type of the response.
	 *
	 * @param response
	 *      the current we response
	 * @param encoding
	 *      the encoding to use
	 */
	public abstract void setContentType(WebResponse response, String encoding);

	/**
	 * Header container component partial page updates.
	 *
	 * @author Matej Knopp
	 */
	private static class PartialHtmlHeaderContainer extends HtmlHeaderContainer
	{
		private static final long serialVersionUID = 1L;

		private transient PartialPageUpdate update;

		/**
		 * Constructor.
		 *
		 * @param update
		 *      the partial page update
		 */
		public PartialHtmlHeaderContainer(final PartialPageUpdate update)
		{
			super(HtmlHeaderSectionHandler.HEADER_ID);
			this.update = update;
		}

		/**
		 *
		 * @see org.apache.wicket.markup.html.internal.HtmlHeaderContainer#newHeaderResponse()
		 */
		@Override
		protected IHeaderResponse newHeaderResponse()
		{
		    if (update != null)
            {
		        return update.getHeaderResponse();
            }
		    
		    return super.newHeaderResponse();
		}
		
		@Override
		protected void onDetach()
		{
		    super.onDetach();
		    update = null;
		}
	}

	/**
	 * Header response for partial updates.
	 *
	 * @author Matej Knopp
	 */
	private class PartialHeaderResponse extends HeaderResponse
	{
		@Override
		public void render(HeaderItem item)
		{
			PriorityHeaderItem priorityHeaderItem = null;
			while (item instanceof IWrappedHeaderItem)
			{
				if (item instanceof PriorityHeaderItem)
				{
					priorityHeaderItem = (PriorityHeaderItem) item;
				}
				item = ((IWrappedHeaderItem) item).getWrapped();
			}

			if (item instanceof OnLoadHeaderItem)
			{
				if (!wasItemRendered(item))
				{
					PartialPageUpdate.this.appendJavaScript(((OnLoadHeaderItem) item).getJavaScript());
					markItemRendered(item);
				}
			}
			else if (item instanceof OnEventHeaderItem)
			{
				if (!wasItemRendered(item))
				{
					PartialPageUpdate.this.appendJavaScript(((OnEventHeaderItem) item).getCompleteJavaScript());
					markItemRendered(item);
				}
			}
			else if (item instanceof OnDomReadyHeaderItem)
			{
				if (!wasItemRendered(item))
				{
					if (priorityHeaderItem != null)
					{
						PartialPageUpdate.this.domReadyJavaScripts.add(0, ((OnDomReadyHeaderItem)item).getJavaScript());
					}
					else
					{
						PartialPageUpdate.this.domReadyJavaScripts.add(((OnDomReadyHeaderItem)item).getJavaScript());
					}
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
	 * Wrapper of a response that buffers its contents.
	 *
	 * @author Igor Vaynberg (ivaynberg)
	 * @author Sven Meier (svenmeier)
	 * 
	 * @see ResponseBuffer#getContents()
	 * @see ResponseBuffer#reset()
	 */
	protected static final class ResponseBuffer extends WebResponse
	{
		private final AppendingStringBuffer buffer = new AppendingStringBuffer(256);

		private final WebResponse originalResponse;

		/**
		 * Constructor.
		 *
		 * @param originalResponse
		 *      the original request cycle response
		 */
		private ResponseBuffer(WebResponse originalResponse)
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
		 * @see org.apache.wicket.request.Response#write(CharSequence)
		 */
		@Override
		public void write(CharSequence cs)
		{
			buffer.append(cs);
		}

		/**
		 * Resets the response to a clean state so it can be reused to save on garbage.
		 */
		@Override
		public void reset()
		{
			buffer.clear();
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

		@Override
		public void addCookie(Cookie cookie)
		{
			originalResponse.addCookie(cookie);
		}

		@Override
		public void clearCookie(Cookie cookie)
		{
			originalResponse.clearCookie(cookie);
		}

		@Override
		public void setHeader(String name, String value)
		{
			originalResponse.setHeader(name, value);
		}

		@Override
		public void addHeader(String name, String value)
		{
			originalResponse.addHeader(name, value);
		}

		@Override
		public void setDateHeader(String name, Time date)
		{
			originalResponse.setDateHeader(name, date);
		}

		@Override
		public void setContentLength(long length)
		{
			originalResponse.setContentLength(length);
		}

		@Override
		public void setContentType(String mimeType)
		{
			originalResponse.setContentType(mimeType);
		}

		@Override
		public void setStatus(int sc)
		{
			originalResponse.setStatus(sc);
		}

		@Override
		public void sendError(int sc, String msg)
		{
			originalResponse.sendError(sc, msg);
		}

		@Override
		public String encodeRedirectURL(CharSequence url)
		{
			return originalResponse.encodeRedirectURL(url);
		}

		@Override
		public void sendRedirect(String url)
		{
			originalResponse.sendRedirect(url);
		}

		@Override
		public boolean isRedirect()
		{
			return originalResponse.isRedirect();
		}

		@Override
		public void flush()
		{
			originalResponse.flush();
		}
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
