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

import javax.annotation.Resource;
import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.ajax.json.JsonFunction;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.JQueryPluginResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * Download resources via Ajax.
 * <p>
 * Usage:
 * 
 * <pre>
 * final AjaxDownload download = new AjaxDownload(resource);
 * add(download);
 * 
 * add(new AjaxButton("download")
 * {
 * 	&#64;Override
 * 	protected void onSubmit(AjaxRequestTarget target, Form&lt;?> form)
 * 	{
 * 		download.initiate(target);
 * 	}
 * });
 * </pre>
 * 
 * @author svenmeier
 */
public class AjaxDownload extends AbstractDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * Name of parameter used to transfer the download identifier to the resource.
	 *
	 * @see #markCompleted(Attributes)
	 */
	private static final String RESOURCE_PARAMETER_NAME = "wicket-ajaxdownload";

	private static final ResourceReference JS = new JQueryPluginResourceReference(
		AjaxDownload.class, "wicket-ajaxdownload.js");

	private final ResourceReference resourceReference;

	private final ResourceBehavior resourceBehavior;

	private PageParameters resourceParameters;

	/**
	 * Download of a {@link Resource}.
	 * 
	 * @param resource
	 *            resource to download
	 */
	public AjaxDownload(IResource resource)
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
	public AjaxDownload(ResourceReference reference)
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
	public AjaxDownload(ResourceReference reference, PageParameters resourceParameters)
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
	 * @param target
	 *            the initiating Ajax target
	 */
	public void initiate(AjaxRequestTarget target)
	{
		if (getComponent() == null)
		{
			throw new WicketRuntimeException("not bound to a component");
		}

		((WebResponse)RequestCycle.get().getResponse()).clearCookie(cookie(getName()));

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
		settings.put("attributes", new JsonFunction(renderAjaxAttributes(getComponent())));
		settings.put("name", getName());
		settings.put("url", url);

		target.appendJavaScript(String.format("Wicket.AjaxDownload.initiate(%s);", settings));

		onBeforeDownload(target);
	}

	protected void onBeforeDownload(AjaxRequestTarget target)
	{
	}

	protected void onDownloadSuccess(AjaxRequestTarget target)
	{
	}

	protected void onDownloadFailed(AjaxRequestTarget target)
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
	private class ResourceBehavior extends Behavior implements IResourceListener
	{
		private final IResource resource;

		private ResourceBehavior(IResource resource)
		{
			this.resource = Args.notNull(resource, "resource");
		}

		@Override
		public void onResourceRequested()
		{
			final RequestCycle requestCycle = RequestCycle.get();
			final Response response = requestCycle.getResponse();
			((WebResponse) response).addCookie(cookie(getName()));

			Attributes a = new Attributes(requestCycle.getRequest(), response, null);

			resource.respond(a);
		}

		public CharSequence getUrl()
		{
			return getComponent().urlFor(this, IResourceListener.INTERFACE, null);
		}
	}

	/**
	 * Mark a resource as complete.
	 * <p>
	 * Has to be called from {@link IResource#respond(Attributes)} when downloaded via
	 * {@link #AjaxDownload(IResource)}.
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
		cookie.setPath("/");
		return cookie;
	}
}
