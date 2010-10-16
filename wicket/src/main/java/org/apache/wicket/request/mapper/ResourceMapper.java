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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * mapper to mount resources to a custom mount path
 * <ul>
 * <li>maps indexed parameters to path segments</li>
 * <li>maps named parameters to query string arguments</li>
 * </ul>
 * 
 * <h4>sample structure of url</h4>
 * 
 * <pre>
 *    /articles/images/[indexed-param-0]/[indexed-param-1]?[named-param-1=value]&[named-param-2=value2]
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
 * @author Peter Ertl
 */
public class ResourceMapper extends AbstractMapper implements IRequestMapper
{
	// path the resource is bound to
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
	 */
	public ResourceMapper(String path, ResourceReference resourceReference)
	{
		Args.notEmpty(path, "path");
		Args.notNull(resourceReference, "resourceReference");
		this.resourceReference = resourceReference;
		mountSegments = getMountSegments(path);
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapRequest(org.apache.wicket.request.Request)
	 */
	public IRequestHandler mapRequest(final Request request)
	{
		Url url = request.getUrl();
		Iterator<String> segments = url.getSegments().iterator();

		// see if url matches the path we are mounted at
		for (String mountSegment : mountSegments)
		{
			if (segments.hasNext() == false)
				return null; // given url is too short

			if (mountSegment.equals(segments.next()) == false)
				return null; // url does not fully match
		}

		// now extract the page parameters from the request url
		PageParameters parameters = new PageParameters();

		// extract indexed parameters
		int index = 0;
		while (segments.hasNext())
			parameters.set(index++, segments.next());

		// extract named parameters
		for (Url.QueryParameter queryParameter : url.getQueryParameters())
			parameters.add(queryParameter.getName(), queryParameter.getValue());

		return new ResourceReferenceRequestHandler(resourceReference, parameters);
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	public int getCompatibilityScore(Request request)
	{
		return 0; // pages always have priority over resources
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapHandler(org.apache.wicket.request.IRequestHandler)
	 */
	public Url mapHandler(IRequestHandler requestHandler)
	{
		if ((requestHandler instanceof ResourceReferenceRequestHandler) == false)
			return null;

		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler)requestHandler;

		// see if request handler addresses the resource we serve
		if (resourceReference.getResource().equals(handler.getResource()) == false)
			return null;

		// create path to resource
		List<String> path = new ArrayList<String>();

		// add mount segments
		path.addAll(Arrays.asList(mountSegments));

		// next we add the page parameters to the resulting path
		PageParameters parameters = handler.getPageParameters();

		// append indexed parameters as path segments
		for (int index = 0; index < parameters.getIndexedCount(); index++)
			path.add(parameters.get(index).toString());

		// append named parameters as query strings
		List<PageParameters.NamedPair> namedParameters = parameters.getAllNamed();
		List<Url.QueryParameter> queryParams = new ArrayList<Url.QueryParameter>(
			namedParameters.size());

		for (PageParameters.NamedPair namedParameter : namedParameters)
			queryParams.add(new Url.QueryParameter(namedParameter.getKey(),
				namedParameter.getValue()));

		// create and return url
		return new Url(path, queryParams);
	}
}
