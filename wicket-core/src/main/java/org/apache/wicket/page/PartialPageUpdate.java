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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.Cookie;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.FeedbackDelay;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.IWrappedHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
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
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A partial update of a page that collects components and header contributions to be written to the
 * client in a specific String-based format (XML, JSON, * ...).
 * <p>
 * The elements of such response are:
 * <ul>
 * <li>component - the markup of the updated component</li>
 * <li>header-contribution - all HeaderItems which have been contributed in any{@link Component#renderHead(IHeaderResponse)},
 * {@link Behavior#renderHead(Component, IHeaderResponse)} or JavaScript explicitly added via {@link #appendJavaScript(CharSequence)}
 * or {@link #prependJavaScript(CharSequence)}</li>
 * </ul>
 */
public abstract class PartialPageUpdate
{
	private static final Logger LOG = LoggerFactory.getLogger(PartialPageUpdate.class);

	/**
	 * Length of the script block that combined scripts are wrapped in. This includes the script tag,
	 * CDATA and if CSP is enabled also the nonce.
	 */
	private static final int SCRIPT_BLOCK_LENGTH = 100;

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
	protected final Map<String, Component> markupIdToComponent = new LinkedHashMap<>();

	/**
	 * A flag that indicates that components cannot be added anymore.
	 * See https://issues.apache.org/jira/browse/WICKET-3564
	 * 
	 * @see #add(Component, String)
	 */
	protected transient boolean componentsFrozen;

	/**
	 * A flag that indicates that javascripts cannot be added anymore.
	 * See https://issues.apache.org/jira/browse/WICKET-6902
	 */
	protected transient boolean javascriptsFrozen;

	/**
	 * Buffer of response body. 
	 */
	protected final ResponseBuffer bodyBuffer;

	/**
	 * Buffer of response header.
	 */
	protected final ResponseBuffer headerBuffer;

	protected HtmlHeaderContainer header = null;
	
	private Component originalHeaderContainer;

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
		this.originalHeaderContainer = page.get(HtmlHeaderSectionHandler.HEADER_ID);
		
		WebResponse response = (WebResponse) page.getResponse();
		bodyBuffer = new ResponseBuffer(response);
		headerBuffer = new ResponseBuffer(response);
	}

	/**
	 * @return returns true if and only if nothing has being added to partial update.
	 */
	public boolean isEmpty()
	{
		return prependJavaScripts.isEmpty() && appendJavaScripts.isEmpty() && domReadyJavaScripts.isEmpty() && markupIdToComponent.isEmpty();
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
		try {
			writeHeader(response, encoding);

			onBeforeRespond(response);

			// process added components
			writeComponents(response, encoding);

			onAfterRespond(response);
			
			javascriptsFrozen = true;

			// queue up prepend javascripts. unlike other steps these are executed out of order so that
			// components can contribute them from during rendering.
			writePriorityEvaluations(response, prependJavaScripts);

			// execute the dom ready javascripts as first javascripts
			// after component replacement
			List<CharSequence> evaluationScripts = new ArrayList<>();
			evaluationScripts.addAll(domReadyJavaScripts);
			evaluationScripts.addAll(appendJavaScripts);
			writeEvaluations(response, evaluationScripts);

			writeFooter(response, encoding);
		} finally {
			if (header != null && originalHeaderContainer!= null) {
				// restore a normal header
				page.replace(originalHeaderContainer);
				header = null;
			}
		}
	}

	/**
	 * Hook-method called before components are written. 
	 * 
	 * @param response
	 */
	protected void onBeforeRespond(Response response) {
	}

	/**
	 * Hook-method called after components are written. 
	 * 
	 * @param response
	 */
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
	 * @param scripts
	 *      the JavaScripts to evaluate
	 */
	protected void writePriorityEvaluations(final Response response, Collection<CharSequence> scripts)
	{
		if (!scripts.isEmpty())
		{
			CharSequence contents = renderScripts(scripts);
			
			writePriorityEvaluation(response, contents);
		}
	}
	
	/**
	 *
	 * @param response
	 *      the response to write to
	 * @param scripts
	 *      the JavaScripts to evaluate
	 */
	protected void writeEvaluations(final Response response, Collection<CharSequence> scripts)
	{
		if (!scripts.isEmpty())
		{
			CharSequence contents = renderScripts(scripts);
			
			writeEvaluation(response, contents);
		}
	}

	private CharSequence renderScripts(Collection<CharSequence> scripts) {
		StringBuilder combinedScript = new StringBuilder(1024);
		for (CharSequence script : scripts)
		{
			combinedScript.append("(function(){").append(script).append("})();");
		}

		StringResponse stringResponse = new StringResponse(combinedScript.length() + SCRIPT_BLOCK_LENGTH);
		IHeaderResponse decoratedHeaderResponse = Application.get().decorateHeaderResponse(new HeaderResponse()
		{
			@Override
			protected Response getRealResponse()
			{
				return stringResponse;
			}
		});
		
		decoratedHeaderResponse.render(JavaScriptHeaderItem.forScript(combinedScript, null));
		decoratedHeaderResponse.close();
		
		return stringResponse.getBuffer();
	}

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

		List<Component> toBeWritten = new ArrayList<>(markupIdToComponent.size());
		
		// delay preparation of feedbacks after all other components
		try (FeedbackDelay delay = new FeedbackDelay(RequestCycle.get())) {
			for (Component component : markupIdToComponent.values())
			{
				if (!containsAncestorFor(component) && prepareComponent(component)) {
					toBeWritten.add(component);
				}
			}

			// .. now prepare all postponed feedbacks
			delay.beforeRender();
		}

		// write components
		for (Component component : toBeWritten)
		{
			writeComponent(response, component.getAjaxRegionMarkupId(), component, encoding);
		}

		if (header != null)
		{
			RequestCycle cycle = RequestCycle.get();
			
			// some header responses buffer all calls to render*** until close is called.
			// when they are closed, they do something (i.e. aggregate all JS resource urls to a
			// single url), and then "flush" (by writing to the real response) before closing.
			// to support this, we need to allow header contributions to be written in the close
			// tag, which we do here:
			headerRendering = true;
			// save old response, set new
			Response oldResponse = cycle.setResponse(headerBuffer);
			headerBuffer.reset();

			// now, close the response (which may render things)
			header.getHeaderResponse().close();

			// revert to old response
			cycle.setResponse(oldResponse);

			// write the XML tags and we're done
			writeHeaderContribution(response, headerBuffer.getContents());
			headerRendering = false;
		}
	}

	/**
	 * Prepare a single component
	 *
	 * @param component
	 *      the component to prepare
	 * @return whether the component was prepared
	 */
	protected boolean prepareComponent(Component component)
	{
		if (component.getRenderBodyOnly())
		{
			throw new IllegalStateException(
					"A partial update is not possible for a component that has renderBodyOnly enabled. Component: " +
							component.toString());
		}

		component.setOutputMarkupId(true);

		// Initialize temporary variables
		final Page parentPage = component.findParent(Page.class);
		if (parentPage == null)
		{
			// dont throw an exception but just ignore this component, somehow
			// it got removed from the page.
			LOG.warn("Component '{}' not rendered because it was already removed from page", component);
			return false;
		}

		try
		{
			component.beforeRender();
		}
		catch (RuntimeException e)
		{
			bodyBuffer.reset();
			throw e;
		}
		
		return true;
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
	protected void writeComponent(Response response, String markupId, Component component, String encoding)
	{
		// substitute our encoding response for the old one so we can capture
		// component's markup in a manner safe for transport inside CDATA block
		Response oldResponse = RequestCycle.get().setResponse(bodyBuffer);

		try
		{
			// render any associated headers of the component
			writeHeaderContribution(response, component);
			
			bodyBuffer.reset();
			
			try
			{
				component.renderPart();
			}
			catch (RuntimeException e)
			{
				bodyBuffer.reset();
				throw e;
			}
		}
		finally
		{
			// Restore original response
			RequestCycle.get().setResponse(oldResponse);
		}

		writeComponent(response, markupId, bodyBuffer.getContents());

		bodyBuffer.reset();
	}

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
	 * Writes a component to the response.
	 *
	 * @param response
	 *      the response to write to
	 * @param contents      
	 * 		the contents
	 */
	protected abstract void writeComponent(Response response, String markupId, CharSequence contents);

	/**
	 * Write priority-evaluation.
	 */
	protected abstract void writePriorityEvaluation(Response response, CharSequence contents);

	/**
	 * Writes a header contribution to the response.
	 *
	 * @param response
	 *      the response to write to
	 * @param contents      
	 * 		the contents
	 */
	protected abstract void writeHeaderContribution(Response response, CharSequence contents);

	/**
	 * Write evaluation.
	 */
	protected abstract void writeEvaluation(Response response, CharSequence contents);

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

		if (javascriptsFrozen)
		{
			throw new IllegalStateException("A partial update of the page is being rendered, JavaScript can no longer be added");
		}

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
		
		if (javascriptsFrozen)
		{
			throw new IllegalStateException("A partial update of the page is being rendered, JavaScript can no longer be added");
		}

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
	{
		Args.notEmpty(markupId, "markupId");
		Args.notNull(component, "component");

		if (component instanceof Page)
		{
			if (component != page)
			{
				throw new IllegalArgumentException("Cannot add another page");
			}
		}
		else
		{
			Page pageOfComponent = component.findParent(Page.class);
			if (pageOfComponent == null) 
			{
				// no longer on page - log the error but don't block the user of the application
				// (which was the behavior in Wicket <= 7).
				LOG.warn("Component '{}' not cannot be updated because it was already removed from page", component);
				return;
			}
			else if (pageOfComponent != page) 
			{
				// on another page
				throw new IllegalArgumentException("Component " + component.toString() + " cannot be updated because it is on another page.");
			}

			if (component instanceof AbstractRepeater)
			{
				throw new IllegalArgumentException(
					"Component " +
					Classes.name(component.getClass()) +
					" is a repeater and cannot be added to a partial page update directly. " +
					"Instead add its parent or another markup container higher in the hierarchy.");
			}
		}

		if (componentsFrozen)
		{
			throw new IllegalStateException("A partial update of the page is being rendered, component " + component.toString() + " can no longer be added");
		}

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
		for (final Component component : markupIdToComponent.values()) {
			final Page parentPage = component.findParent(Page.class);
			if (parentPage != null) {
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
		return markupIdToComponent.containsValue(page);
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
			page.addOrReplace(header);
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

		// note: in almost all cases the header will be empty here,
		// since all header items will be rendered later on close only
		writeHeaderContribution(response, headerBuffer.getContents());
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
	 * Header container component for partial page updates.
	 * <p>
	 * This container is temporarily injected into the page to provide the
	 * {@link IHeaderResponse} while components are rendered. It is never
	 * rendered itself. 
	 *
	 * @author Matej Knopp
	 */
	private static class PartialHtmlHeaderContainer extends HtmlHeaderContainer
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Keep transiently, in case the containing page gets serialized before
		 * this container is removed again. This happens when DebugBar determines
		 * the page size by serializing/deserializing it.
		 */
		private transient PartialPageUpdate pageUpdate;

		/**
		 * Constructor.
		 *
		 * @param pageUpdate
		 *      the partial page update
		 */
		public PartialHtmlHeaderContainer(PartialPageUpdate pageUpdate)
		{
			super(HtmlHeaderSectionHandler.HEADER_ID);

			this.pageUpdate = pageUpdate;
		}

		/**
		 *
		 * @see org.apache.wicket.markup.html.internal.HtmlHeaderContainer#newHeaderResponse()
		 */
		@Override
		protected IHeaderResponse newHeaderResponse()
		{
			if (pageUpdate == null) {
				throw new IllegalStateException("disconnected from pageUpdate after serialization");
			}

	        return pageUpdate.getHeaderResponse();
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
			while (item instanceof IWrappedHeaderItem)
			{
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
					PartialPageUpdate.this.domReadyJavaScripts.add(((OnDomReadyHeaderItem)item).getJavaScript());
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
		public boolean isHeaderSupported()
		{
			return originalResponse.isHeaderSupported();
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
		public void setDateHeader(String name, Instant date)
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
}
