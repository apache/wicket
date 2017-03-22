/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.wicket.extensions.behavior;

import java.util.UUID;

import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * The idea of this class came from here: <a href="https://cwiki.apache.org/confluence/display/WICKET/AJAX+update+and+file+download+in+one+blow">
 * https://cwiki.apache.org/confluence/display/WICKET/AJAX+update+and+file+download+in+one+blow</href>
 * 
 * here is the basic usage example:
 * 
 * final AjaxDownload download = new AjaxDownload();
 * add(download);
 * 
 * final Form<Void> form = new Form<>("form");
 * form.add(new AjaxButton("page-download") {
 *     private static final long serialVersionUID = 1L;
 * 
 *     @Override
 *     protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 *         download.setFileName("test.txt");
 *         download.setResourceStream(new StringResourceStream("bla-bla-bla", "text/plain"));
 *         download.initiate(target);
 *     }
 * 
 *     @Override
 *     protected void onError(AjaxRequestTarget target, Form<?> form) {
 *     }
 * });
 */
public class AjaxDownload extends Behavior implements IResourceListener
{
	private static final long serialVersionUID = 1L;
	private boolean addAntiCache;
	private Component component;
	private String fileName;
	private IResourceStream resourceStream;
	private final String iframeId;

	public AjaxDownload()
	{
		this(true);
	}

	public AjaxDownload(boolean addAntiCache)
	{
		super();
		this.addAntiCache = addAntiCache;
		iframeId = String.format("download-iframe-%s", UUID.randomUUID().toString());
	}

	/**
	 * Call this method to initiate the download.
	 */
	public void initiate(AjaxRequestTarget target)
	{
		PageParameters pp = new PageParameters();

		if (addAntiCache)
		{
			pp.add("antiCache", System.currentTimeMillis());
		}
		String url = component.urlFor(this, IResourceListener.INTERFACE, pp).toString();
		target.appendJavaScript(String.format("$('#%s').attr('src', '%s');", iframeId, url));
	}

	@Override
	public void bind(Component component)
	{
		this.component = component;
		if (!(component instanceof Page))
		{
			component.setOutputMarkupId(true);
		}
	}

	@Override
	public void unbind(Component component)
	{
		this.component = null;
		super.unbind(component);
	}

	private static ResourceReference newResourceReference()
	{
		return new JavaScriptResourceReference(AjaxDownload.class, "ajax-download.js");
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(newResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript(String.format("addDwnldIframe('%s', '%s');", component instanceof Page ? "" : component.getMarkupId(), iframeId)));
	}

	@Override
	public void onResourceRequested()
	{
		ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(getResourceStream(), getFileName());
		handler.setContentDisposition(getContentDisposition());
		component.getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
	}

	/**
	 * Override this method to change ContentDisposition, default implementation 
	 * return ATTACHMENT
	 * 
	 * @return ATTACHMENT
	 */
	protected ContentDisposition getContentDisposition()
	{
		return ContentDisposition.ATTACHMENT;
	}
	/**
	 * Override this method for a file name which will let the browser prompt
	 * with a save/open dialog.
	 * 
	 * @see ResourceStreamRequestTarget#getFileName()
	 */
	protected String getFileName()
	{
		return this.fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	/**
	 * Hook method providing the actual resource stream.
	 */
	protected IResourceStream getResourceStream()
	{
		return resourceStream;

	}

	public void setResourceStream(IResourceStream resourceStream)
	{
		this.resourceStream = resourceStream;
	}
}
