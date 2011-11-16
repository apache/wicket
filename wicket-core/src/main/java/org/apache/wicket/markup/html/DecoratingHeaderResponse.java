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

import org.apache.wicket.request.Response;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * This is simply a helper implementation of IHeaderResponse that really delegates all of its method
 * calls to the IHeaderResponse that is passed into the constructor. It is defined as abstract
 * because it's only meant to be extended and not used a la carte. You can extend it and override
 * only the methods that you want to change the functionality of.
 * 
 * @see IHeaderResponseDecorator
 * @see IHeaderResponse
 * @author Jeremy Thomerson
 */
public abstract class DecoratingHeaderResponse implements IHeaderResponse
{

	private final IHeaderResponse realResponse;

	/**
	 * Create a header response that simply delegates all methods to the one that is passed in here.
	 * 
	 * @param real
	 *            the actual response that this class delegates to by default
	 */
	public DecoratingHeaderResponse(IHeaderResponse real)
	{
		realResponse = real;
	}

	/**
	 * Returns the actual response being decorated for subclasses to be able to pass it off to other
	 * objects if they need to do so.
	 * 
	 * @return the actual wrapped IHeaderResponse
	 */
	protected final IHeaderResponse getRealResponse()
	{
		return realResponse;
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference)
	{
		realResponse.renderJavaScriptReference(reference);
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference, String id)
	{
		realResponse.renderJavaScriptReference(reference, id);
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference,
		PageParameters pageParameters, String id)
	{
		realResponse.renderJavaScriptReference(reference, pageParameters, id);
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference,
		PageParameters pageParameters, String id, boolean defer)
	{
		realResponse.renderJavaScriptReference(reference, pageParameters, id, defer);
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference,
		PageParameters pageParameters, String id, boolean defer, String charset)
	{
		realResponse.renderJavaScriptReference(reference, pageParameters, id, defer, charset);
	}

	@Override
	public void renderJavaScriptReference(String url)
	{
		realResponse.renderJavaScriptReference(url);
	}

	@Override
	public void renderJavaScriptReference(String url, String id)
	{
		realResponse.renderJavaScriptReference(url, id);
	}

	@Override
	public void renderJavaScriptReference(String url, String id, boolean defer)
	{
		realResponse.renderJavaScriptReference(url, id, defer);
	}

	@Override
	public void renderJavaScriptReference(String url, String id, boolean defer, String charset)
	{
		realResponse.renderJavaScriptReference(url, id, defer, charset);
	}

	@Override
	public void renderJavaScript(CharSequence javascript, String id)
	{
		realResponse.renderJavaScript(javascript, id);
	}

	@Override
	public void renderCSSReference(ResourceReference reference)
	{
		realResponse.renderCSSReference(reference);
	}

	@Override
	public void renderCSS(CharSequence css, String id)
	{
		realResponse.renderCSS(css, id);
	}

	@Override
	public void renderCSSReference(String url)
	{
		realResponse.renderCSSReference(url);
	}

	@Override
	public void renderCSSReference(ResourceReference reference, String media)
	{
		realResponse.renderCSSReference(reference, media);
	}

	@Override
	public void renderCSSReference(ResourceReference reference, PageParameters pageParameters,
		String media)
	{
		realResponse.renderCSSReference(reference, pageParameters, media);
	}

	@Override
	public void renderCSSReference(ResourceReference reference, PageParameters pageParameters,
		String media, String condition)
	{
		realResponse.renderCSSReference(reference, pageParameters, media, condition);
	}

	@Override
	public void renderCSSReference(String url, String media)
	{
		realResponse.renderCSSReference(url, media);
	}

	@Override
	public void renderCSSReference(String url, String media, String condition)
	{
		realResponse.renderCSSReference(url, media, condition);
	}

	@Override
	public void renderString(CharSequence string)
	{
		realResponse.renderString(string);
	}

	@Override
	public void markRendered(Object object)
	{
		realResponse.markRendered(object);
	}

	@Override
	public boolean wasRendered(Object object)
	{
		return realResponse.wasRendered(object);
	}

	@Override
	public Response getResponse()
	{
		return realResponse.getResponse();
	}

	@Override
	public void renderOnDomReadyJavaScript(String javascript)
	{
		realResponse.renderOnDomReadyJavaScript(javascript);
	}

	@Override
	public void renderOnLoadJavaScript(String javascript)
	{
		realResponse.renderOnLoadJavaScript(javascript);
	}

	@Override
	public void renderOnEventJavaScript(String target, String event, String javascript)
	{
		realResponse.renderOnEventJavaScript(target, event, javascript);
	}

	@Override
	public void close()
	{
		realResponse.close();
	}

	@Override
	public boolean isClosed()
	{
		return realResponse.isClosed();
	}
}
