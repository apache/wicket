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
package org.apache.wicket.request.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * mapper to mount resources to a custom mount path
 * <ul>
 * <li>maps indexed parameters to path segments</li>
 * <li>maps named parameters to query string arguments or placeholder path segments</li>
 * </ul>
 * 
 * <strong>sample structure of url</strong>
 * 
 * <pre>
 *    /resources/${category}/images/[indexed-param-0]/[indexed-param-1]?[named-param-1=value]&[named-param-2=value2]
 * </pre>
 * 
 * <h4>sample usage</h4>
 * 
 * in your wicket application's init() method use a statement like this
 * <p/>
 * 
 * <pre>
 * getRootRequestMapperAsCompound().add(new ResourceMapper(&quot;/images&quot;, new ImagesResourceReference()));
 * </pre>
 * 
 * @see org.apache.wicket.protocol.http.WebApplication#mountResource(String,
 *      org.apache.wicket.request.resource.ResourceReference)
 * 
 * @author Peter Ertl
 */
public class ResourceMapper extends AbstractMapper implements IRequestMapper
{
	// encode page parameters into url + decode page parameters from url
	private final IPageParametersEncoder parametersEncoder;

	// mount path (= segments) the resource is bound to
	private final String[] mountSegments;

	// resource that the mapper links to
	private final ResourceReference resourceReference;

	/**
	 * create a resource mapper for a resource
	 * 
	 * @param path
	 *            mount path for the resource
	 * @param resourceReference
	 *            resource reference that should be linked to the mount path
	 * 
	 * @see #ResourceMapper(String, org.apache.wicket.request.resource.ResourceReference,
	 *      org.apache.wicket.request.mapper.parameter.IPageParametersEncoder)
	 */
	public ResourceMapper(String path, ResourceReference resourceReference)
	{
		this(path, resourceReference, new PageParametersEncoder());
	}

	/**
	 * create a resource mapper for a resource
	 * 
	 * @param path
	 *            mount path for the resource
	 * @param resourceReference
	 *            resource reference that should be linked to the mount path
	 * @param encoder
	 *            encoder for url parameters
	 */
	public ResourceMapper(String path, ResourceReference resourceReference,
		IPageParametersEncoder encoder)
	{
		Args.notEmpty(path, "path");
		Args.notNull(resourceReference, "resourceReference");
		Args.notNull(encoder, "encoder");

		this.resourceReference = resourceReference;
		mountSegments = getMountSegments(path);
		parametersEncoder = encoder;
	}

	public IRequestHandler mapRequest(final Request request)
	{
		final Url url = request.getUrl();

		// check if url matches mount path
		if (urlStartsWith(url, mountSegments) == false)
		{
			return null;
		}

		// now extract the page parameters from the request url
		PageParameters parameters = extractPageParameters(request, mountSegments.length,
			parametersEncoder);

		// check if there are placeholders in mount segments
		for (int index = 0; index < mountSegments.length; ++index)
		{
			String placeholder = getPlaceholder(mountSegments[index]);

			if (placeholder != null)
			{
				// extract the parameter from URL
				if (parameters == null)
				{
					parameters = new PageParameters();
				}
				parameters.add(placeholder, url.getSegments().get(index));
			}
		}
		return new ResourceReferenceRequestHandler(resourceReference, parameters);
	}

	public int getCompatibilityScore(Request request)
	{
		return 0; // pages always have priority over resources
	}

	public Url mapHandler(IRequestHandler requestHandler)
	{
		if ((requestHandler instanceof ResourceReferenceRequestHandler) == false)
		{
			return null;
		}

		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler)requestHandler;

		// see if request handler addresses the resource we serve
		if (resourceReference.equals(handler.getResourceReference()) == false &&
			resourceReference.getResource().equals(handler.getResource()) == false)
		{
			return null;
		}

		Url url = new Url();

		// add mount path segments
		for (String segment : mountSegments)
		{
			url.getSegments().add(segment);
		}

		// replace placeholder parameters
		PageParameters parameters = new PageParameters(handler.getPageParameters());

		for (int index = 0; index < mountSegments.length; ++index)
		{
			String placeholder = getPlaceholder(mountSegments[index]);

			if (placeholder != null)
			{
				url.getSegments().set(index, parameters.get(placeholder).toString(""));
				parameters.remove(placeholder);
			}
		}

		// create url
		return encodePageParameters(url, parameters, parametersEncoder);
	}
}
