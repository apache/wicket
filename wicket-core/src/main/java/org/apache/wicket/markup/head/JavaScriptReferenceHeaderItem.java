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
import java.util.List;

import org.apache.wicket.markup.html.CrossOrigin;
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
	private static final long serialVersionUID = 1L;
	
	private final ResourceReference reference;
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
	 */
	public JavaScriptReferenceHeaderItem(ResourceReference reference, PageParameters pageParameters, String id)
	{
		this.reference = Args.notNull(reference, "reference");
		this.pageParameters = pageParameters;
		setId(id);
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
	 * @return the parameters for this Javascript resource reference
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	@Override
	public List<HeaderItem> getDependencies()
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
		internalRenderJavaScriptReference(response, getUrl());
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
		// Not using `Objects.hash` for performance reasons
		int result = super.hashCode();
		result = 31 * result + (reference != null ? reference.hashCode() : 0);
		result = 31 * result + (pageParameters != null ? pageParameters.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		JavaScriptReferenceHeaderItem that = (JavaScriptReferenceHeaderItem) o;
		return java.util.Objects.equals(reference, that.reference) &&
				java.util.Objects.equals(pageParameters, that.pageParameters);
	}
	
	@Override 
	public String getIntegrity()
	{
		String tempIntegrity = super.getIntegrity();
		if (tempIntegrity == null)
		{
			tempIntegrity = reference.getIntegrity();
		}
		return tempIntegrity;
	}
	
	@Override
	public CrossOrigin getCrossOrigin()
	{
		CrossOrigin tempOrigin = super.getCrossOrigin();
		if (tempOrigin == null)
		{
			tempOrigin = reference.getCrossOrigin();
		}
		return tempOrigin;
	}	
}
