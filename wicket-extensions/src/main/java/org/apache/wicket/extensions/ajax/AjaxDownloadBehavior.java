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
package org.apache.wicket.extensions.ajax;

import java.util.Locale;
import javax.servlet.http.Cookie;
import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.json.JSONFunction;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.JQueryPluginResourceReference;
import org.apache.wicket.util.lang.Args;

import com.github.openjson.JSONObject;

/**
 * Download resources via Ajax.
 * <p>
 * Usage:
 *
 * <pre>
 * final AjaxDownloadBehavior download = new AjaxDownloadBehavior(resource);
 * add(download);
 *
 * add(new AjaxButton("download")
 * {
 * 	&#64;Override
 * 	protected void onSubmit(IPartialPageRequestHandler handler, Form&lt;?&gt; form)
 * 	{
 * 		download.initiate(handler);
 * 	}
 * });
 * </pre>
 *
 * <p>To set the name of the downloaded resource make use of
 * {@link org.apache.wicket.request.resource.ResourceStreamResource#setFileName(String)} or
 * {@link org.apache.wicket.request.resource.AbstractResource.ResourceResponse#setFileName(String)}</p>
 *
 * @author svenmeier
 * @author Martin Grigorov
 * @author Maxim Solodovnik
 */
public class AjaxDownloadBehavior extends AbstractDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	public enum Location {
		/**
		 * The resource will be downloaded into a {@code blob}.
		 * <p>
		 * This is recommended for modern browsers.
		 */
		Blob,

		/**
		 * The resource will be downloaded via a temporary created iframe, the resource has to be a
		 * {@link ContentDisposition#ATTACHMENT}.
		 * <p>
		 * This is recommended when there are resources in the DOM which will be
		 * closed automatically on JavaScript <em>unload</em> event, like WebSockets.
		 * Supports both <em>success</em> and <em>failure</em> callbacks!
		 */
		IFrame,

		/**
		 * The resource will be downloaded by changing the location of the current DOM document,
		 * the resource has to be a {@link ContentDisposition#ATTACHMENT}.
		 * <p>
		 * Note: This will trigger JavaScript <em>unload</em> event on the page!
		 * Does not support {@link AjaxDownloadBehavior#onDownloadFailed(AjaxRequestTarget)} callback,
		 * i.e. it is not possible to detect when the download has failed!
		 */
		SameWindow,

		/**
		 * The resource will be downloaded in a new browser window by using JavaScript <code>window.open()</code> API,
		 * the resource has to be a {@link ContentDisposition#INLINE}.
		 */
		NewWindow
	}

	/**
	 * Name of parameter used to transfer the download identifier to the resource.
	 *
	 * @see #markCompleted(Attributes)
	 */
	private static final String RESOURCE_PARAMETER_NAME = "wicket-ajaxdownload";

	private static final ResourceReference JS = new JQueryPluginResourceReference(
		AjaxDownloadBehavior.class, "wicket-ajaxdownload.js");

	private final ResourceReference resourceReference;

	private final ResourceBehavior resourceBehavior;

	private PageParameters resourceParameters;

	private Location location = Location.Blob;

	/**
	 * Download of a {@link IResource}.
	 *
	 * @param resource
	 *            resource to download
	 */
	public AjaxDownloadBehavior(IResource resource)
	{
		Args.notNull(resource, "resource");
		this.resourceBehavior = new ResourceBehavior(resource);
		this.resourceReference = null;
	}

	/**
	 * Download of a {@link ResourceReference}.
	 * <p>
	 * The {@link IResource} returned by {@link ResourceReference#getResource()} must call
	 * {@link #markCompleted(Attributes)} when responding, otherwise the callback
	 * {@link #onDownloadSuccess(AjaxRequestTarget)} will not work.
	 *
	 * @param reference
	 *            reference to resource to download
	 */
	public AjaxDownloadBehavior(ResourceReference reference)
	{
		this(reference, null);
	}

	/**
	 * Download of a {@link ResourceReference}.
	 * <p>
	 * The {@link IResource} returned by {@link ResourceReference#getResource()} must call
	 * {@link #markCompleted(Attributes)} when responding, otherwise the callback
	 * {@link #onDownloadSuccess(AjaxRequestTarget)} will not work.
	 *
	 * @param reference
	 *            reference to resource to download
	 * @param resourceParameters
	 *            parameters for the resource
	 */
	public AjaxDownloadBehavior(ResourceReference reference, PageParameters resourceParameters)
	{
		this.resourceBehavior = null;

		this.resourceReference = Args.notNull(reference, "reference");
		this.resourceParameters = resourceParameters;
	}

	@Override
	protected void onBind()
	{
		super.onBind();

		if (resourceBehavior != null)
		{
			getComponent().add(resourceBehavior);
		}
	}

	@Override
	protected void onUnbind()
	{
		super.onUnbind();

		if (resourceBehavior != null)
		{
			getComponent().remove(resourceBehavior);
		}
	}

	/**
	 * Call this method to initiate the download.
	 *
	 * @param handler
	 *            the initiating RequestHandler
	 */
	public void initiate(IPartialPageRequestHandler handler)
	{
		if (getComponent() == null)
		{
			throw new WicketRuntimeException("not bound to a component");
		}

		CharSequence url;
		if (resourceBehavior == null)
		{
			if (resourceReference.canBeRegistered())
			{
				getComponent().getApplication().getResourceReferenceRegistry()
					.registerResourceReference(resourceReference);
			}

			PageParameters parameters = new PageParameters();
			if (resourceParameters != null)
			{
				parameters.mergeWith(resourceParameters);
			}
			parameters.set(RESOURCE_PARAMETER_NAME, getName());

			url = getComponent().getRequestCycle()
				.urlFor(new ResourceReferenceRequestHandler(resourceReference, parameters));
		}
		else
		{
			url = resourceBehavior.getUrl();
		}

		JSONObject settings = new JSONObject();
		settings.put("attributes", new JSONFunction(renderAjaxAttributes(getComponent())));
		settings.put("name", getName());
		settings.put("downloadUrl", url);
		settings.put("method", getLocation().name().toLowerCase(Locale.ROOT));

		handler.appendJavaScript(String.format("Wicket.AjaxDownload.initiate(%s);", settings));

		onBeforeDownload(handler);
	}

	protected void onBeforeDownload(IPartialPageRequestHandler handler)
	{
	}

	/**
	 * A callback executed when the download of the resource finished successfully.
	 *
	 * @param target The Ajax request handler
	 */
	protected void onDownloadSuccess(AjaxRequestTarget target)
	{
	}

	/**
	 * A callback executed when the download of the resource failed for some reason,
	 * e.g. an error at the server side.
	 * <p>
	 * Since the HTTP status code of the download is not available to Wicket, any HTML in the resource response
	 * will be interpreted as a failure HTTP status message. Thus is it not possible to download HTML resources
	 * via {@link AjaxDownloadBehavior}.
	 *
	 * @param target The Ajax request handler
	 */
	protected void onDownloadFailed(AjaxRequestTarget target)
	{
	}

	/**
	 * A callback executed when the download of the resource finished successfully or with a failure.
	 *
	 * @param target The Ajax request handler
	 */
	protected void onDownloadCompleted(AjaxRequestTarget target)
	{
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(JS));
	}

	@Override
	protected void respond(AjaxRequestTarget target)
	{
		String result = getComponent().getRequest().getRequestParameters().getParameterValue("result").toOptionalString();
		if ("success".equals(result)) {
			onDownloadSuccess(target);
		} else if ("failed".equals(result)) {
			onDownloadFailed(target);
		}
		onDownloadCompleted(target);
	}

	public Location getLocation() {
		return location;
	}

	public AjaxDownloadBehavior setLocation(final Location location) {
		this.location = Args.notNull(location, "location");
		return this;
	}

	/**
	 * Identifying name of this behavior.
	 */
	private String getName()
	{
		return String.format("wicket-ajaxdownload-%s-%s", getComponent().getMarkupId(),
			getComponent().getBehaviorId(this));
	}

	/**
	 * The behavior responding with the actual resource.
	 */
	private class ResourceBehavior extends Behavior implements IRequestListener
	{
		private static final long serialVersionUID = 1L;
		private final IResource resource;

		private ResourceBehavior(IResource resource)
		{
			this.resource = Args.notNull(resource, "resource");
		}

		@Override
		public boolean rendersPage()
		{
			return false;
		}

		@Override
		public void onRequest()
		{
			final RequestCycle requestCycle = RequestCycle.get();
			final Response response = requestCycle.getResponse();
			((WebResponse) response).addCookie(cookie(getName()));

			Attributes a = new Attributes(requestCycle.getRequest(), response, null);

			resource.respond(a);
		}

		public CharSequence getUrl()
		{
			return getComponent().urlForListener(this, null);
		}
	}

	/**
	 * Mark a resource as complete.
	 * <p>
	 * Has to be called from {@link IResource#respond(Attributes)} when downloaded via
	 * {@link #AjaxDownloadBehavior(IResource)}.
	 *
	 * @param attributes
	 *            resource attributes
	 */
	public static void markCompleted(IResource.Attributes attributes)
	{
		String cookieName = attributes.getParameters().get(RESOURCE_PARAMETER_NAME).toString();

		((WebResponse)attributes.getResponse()).addCookie(cookie(cookieName));
	}

	private static Cookie cookie(String name)
	{
		Cookie cookie = new Cookie(name, "complete");

		// has to be on root, otherwise JavaScript will not be able to access the
		// cookie when it is set from a different path - which is the case when a
		// ResourceReference is used
		cookie.setPath("/");

		return cookie;
	}
}
