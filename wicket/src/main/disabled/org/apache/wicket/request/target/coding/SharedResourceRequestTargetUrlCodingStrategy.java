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
package org.apache.wicket.request.target.coding;

import org.apache.wicket.IRequestHandler;
import org.apache.wicket.request.ObsoleteRequestParameters;
import org.apache.wicket.request.target.resource.ISharedResourceRequestTarget;
import org.apache.wicket.request.target.resource.SharedResourceRequestTarget;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.value.ValueMap;

/**
 * Encodes and decodes mounts for a single resource class.
 * 
 * @author Gili Tzabari
 */
public class SharedResourceRequestTargetUrlCodingStrategy extends
	AbstractRequestTargetUrlCodingStrategy
{
	private final String resourceKey;


	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param resourceKey
	 */
	public SharedResourceRequestTargetUrlCodingStrategy(final String mountPath,
		final String resourceKey)
	{
		super(mountPath);
		this.resourceKey = resourceKey;
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#decode(org.apache.wicket.request.ObsoleteRequestParameters)
	 */
	public IRequestHandler decode(ObsoleteRequestParameters requestParameters)
	{
		final String parametersFragment = requestParameters.getPath().substring(
			getMountPath().length());
		final ValueMap parameters = decodeParameters(parametersFragment,
			requestParameters.getParameters());

		requestParameters.setParameters(parameters);
		requestParameters.setResourceKey(resourceKey);
		return new SharedResourceRequestTarget(requestParameters);
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#encode(org.apache.wicket.IRequestHandler)
	 */
	public CharSequence encode(IRequestHandler requestTarget)
	{
		if (!(requestTarget instanceof ISharedResourceRequestTarget))
		{
			throw new IllegalArgumentException("This encoder can only be used with " +
				"instances of " + ISharedResourceRequestTarget.class.getName());
		}
		final AppendingStringBuffer url = new AppendingStringBuffer(40);
		url.append(getMountPath());
		final ISharedResourceRequestTarget target = (ISharedResourceRequestTarget)requestTarget;

		ObsoleteRequestParameters requestParameters = target.getRequestParameters();
		appendParameters(url, requestParameters.getParameters());
		return url;
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#matches(org.apache.wicket.IRequestHandler)
	 */
	public boolean matches(IRequestHandler requestTarget)
	{
		if (requestTarget instanceof ISharedResourceRequestTarget)
		{
			ISharedResourceRequestTarget target = (ISharedResourceRequestTarget)requestTarget;
			return target.getRequestParameters().getResourceKey().equals(resourceKey);
		}
		else
		{
			return false;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "SharedResourceEncoder[key=" + resourceKey + "]";
	}
}