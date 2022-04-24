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
import java.util.List;
import java.util.Objects;

import org.apache.wicket.markup.html.CrossOrigin;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.bundles.IResourceBundle;
import org.apache.wicket.util.string.Strings;

/**
 * {@link HeaderItem} for style tags that are rendered using a {@link ResourceReference}.
 * 
 * @author papegaaij
 */
public class CssReferenceHeaderItem extends AbstractCssReferenceHeaderItem implements IReferenceHeaderItem
{
	private static final long serialVersionUID = 1L;

	private final ResourceReference reference;
	private final PageParameters pageParameters;

	/**
	 * Creates a new {@code CSSReferenceHeaderItem}.
	 * 
	 * @param reference
	 *            resource reference pointing to the CSS resource
	 * @param pageParameters
	 *            the parameters for this CSS resource reference
	 * @param media
	 *            the media type for this CSS ("print", "screen", etc.)
	 */
	public CssReferenceHeaderItem(ResourceReference reference, PageParameters pageParameters,
		String media)
	{
		super(media, null);
		
		this.reference = reference;
		this.pageParameters = pageParameters;
	}

	/**
	 * Creates a new {@code CSSReferenceHeaderItem}.
	 * 
	 * @param reference
	 *            resource reference pointing to the CSS resource
	 * @param pageParameters
	 *            the parameters for this CSS resource reference
	 * @param media
	 *            the media type for this CSS ("print", "screen", etc.)
	 * @param rel
	 *            the rel attribute content
	 */
	public CssReferenceHeaderItem(ResourceReference reference, PageParameters pageParameters,
		String media, String rel)
	{
		super(media, rel); 
		
		this.reference = reference;
		this.pageParameters = pageParameters;
	}

	/**
	 * @return resource reference pointing to the CSS resource
	 * @see IReferenceHeaderItem#getReference()
	 */
	@Override
	public ResourceReference getReference()
	{
		return reference;
	}

	/**
	 * @return the parameters for this CSS resource reference
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	@Override
	public CrossOrigin getCrossOrigin()
	{
		return null;
	}
	
	@Override
	public String getIntegrity()
	{
		return null;
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
		internalRenderCSSReference(response, getUrl());
	}

	@Override
	public Iterable<?> getRenderTokens()
	{
		return Arrays.asList("css-" + Strings.stripJSessionId(getUrl()) + "-" + getMedia());
	}

	@Override
	public String toString()
	{
		return "CSSReferenceHeaderItem(" + getReference() + ", " + getPageParameters() + ")";
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
		int result = super.hashCode();
		result = 31 * result + ((reference != null) ? reference.hashCode() : 0);
		result = 31 * result + ((getMedia() != null) ? getMedia().hashCode() : 0);
		result = 31 * result + ((pageParameters != null) ? pageParameters.hashCode() : 0);
		result = 31 * result + ((getRel() != null) ? getRel().hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		CssReferenceHeaderItem that = (CssReferenceHeaderItem)o;
		return Objects.equals(reference, that.reference) && Objects.equals(getMedia(), that.getMedia()) &&
			Objects.equals(getRel(), that.getRel()) && Objects.equals(pageParameters, that.pageParameters);
	}
}
