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
package org.apache.wicket.markup.head;

import java.util.Arrays;
import java.util.Collections;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.bundles.IResourceBundle;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * {@link HeaderItem} for script tags that are rendered using a {@link ResourceReference}.
 * 
 * @author papegaaij
 */
public class JavaScriptReferenceHeaderItem extends AbstractJavaScriptReferenceHeaderItem
	implements
		IReferenceHeaderItem
{
	private final ResourceReference reference;
	private final String id;
	private final PageParameters pageParameters;

	/**
	 * Creates a new {@code JavaScriptReferenceHeaderItem}.
	 * 
	 * @param reference
	 *            resource reference pointing to the javascript resource
	 * @param pageParameters
	 *            the parameters for this Javascript resource reference
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 * @param defer
	 *            specifies that the execution of a script should be deferred (delayed) until after
	 *            the page has been loaded.
	 * @param charset
	 *            a non null value specifies the charset attribute of the script tag
	 * @param condition
	 *            the condition to use for Internet Explorer conditional comments. E.g. "IE 7".
	 */
	public JavaScriptReferenceHeaderItem(ResourceReference reference,
		PageParameters pageParameters, String id, boolean defer, String charset, String condition)
	{
		super(condition, defer, charset);
		this.reference = Args.notNull(reference, "reference");
		this.pageParameters = pageParameters;
		this.id = id;
	}

	/**
	 * @return the resource reference pointing to the javascript resource
	 */
	@Override
	public ResourceReference getReference()
	{
		return reference;
	}

	/**
	 * @return the id that will be used to filter duplicate reference
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return the parameters for this Javascript resource reference
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies()
	{
		return getReference().getDependencies();
	}

	@Override
	public Iterable<? extends HeaderItem> getProvidedResources()
	{
		if (getReference() instanceof IResourceBundle)
			return ((IResourceBundle)getReference()).getProvidedResources();
		return super.getProvidedResources();
	}

	@Override
	public void render(Response response)
	{
		internalRenderJavaScriptReference(response, getUrl(), getId(), isDefer(), getCharset(),
			getCondition(), isAsync());
	}

	@Override
	public Iterable<?> getRenderTokens()
	{
		String url = Strings.stripJSessionId(getUrl());
		if (Strings.isEmpty(getId()))
			return Collections.singletonList("javascript-" + url);
		else
			return Arrays.asList("javascript-" + getId(), "javascript-" + url);
	}

	@Override
	public String toString()
	{
		return "JavaScriptReferenceHeaderItem(" + getReference() + ", " + getPageParameters() + ')';
	}

	private String getUrl()
	{
		IRequestHandler handler = new ResourceReferenceRequestHandler(getReference(),
			getPageParameters());
		return RequestCycle.get().urlFor(handler).toString();
	}

	@Override
	public int hashCode()
	{
		return getReference().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof JavaScriptReferenceHeaderItem)
			return ((JavaScriptReferenceHeaderItem)obj).getReference().equals(getReference());
		return false;
	}
}
