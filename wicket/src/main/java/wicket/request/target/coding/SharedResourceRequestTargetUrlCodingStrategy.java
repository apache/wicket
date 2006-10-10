/*
 * $Id: BookmarkablePageRequestTargetUrlCodingStrategy.java,v 1.1 2005/12/10
 * 21:28:56 eelco12 Exp $ $Revision: 5032 $ $Date: 2006-03-19 11:17:16 -0500
 * (Sun, 19 Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.request.target.coding;

import wicket.IRequestTarget;
import wicket.request.RequestParameters;
import wicket.request.target.resource.ISharedResourceRequestTarget;
import wicket.request.target.resource.SharedResourceRequestTarget;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.value.ValueMap;

/**
 * Encodes and decodes mounts for a single resource class.
 * 
 * @author Gili Tzabari
 */
public class SharedResourceRequestTargetUrlCodingStrategy
		extends
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
	 * @see wicket.request.target.coding.IRequestTargetUrlCodingStrategy#decode(wicket.request.RequestParameters)
	 */
	public IRequestTarget decode(RequestParameters requestParameters)
	{
		final String parametersFragment = requestParameters.getPath().substring(
				getMountPath().length());
		final ValueMap parameters = decodeParameters(parametersFragment, requestParameters
				.getParameters());

		requestParameters.setParameters(parameters);
		requestParameters.setResourceKey(resourceKey);
		return new SharedResourceRequestTarget(requestParameters);
	}

	/**
	 * @see wicket.request.target.coding.IRequestTargetUrlCodingStrategy#matches(wicket.IRequestTarget)
	 */
	public boolean matches(IRequestTarget requestTarget)
	{
		if (requestTarget instanceof ISharedResourceRequestTarget)
		{
			SharedResourceRequestTarget target = (SharedResourceRequestTarget)requestTarget;
			return target.getRequestParameters().getResourceKey().equals(resourceKey);
		}
		else
		{
			return false;
		}
	}

	/**
	 * @see wicket.request.target.coding.IRequestTargetUrlCodingStrategy#encode(wicket.IRequestTarget)
	 */
	public CharSequence encode(IRequestTarget requestTarget)
	{
		if (!(requestTarget instanceof ISharedResourceRequestTarget))
		{
			throw new IllegalArgumentException("This encoder can only be used with "
					+ "instances of " + ISharedResourceRequestTarget.class.getName());
		}
		final AppendingStringBuffer url = new AppendingStringBuffer(40);
		url.append(getMountPath());
		final ISharedResourceRequestTarget target = (ISharedResourceRequestTarget)requestTarget;

		RequestParameters requestParameters = target.getRequestParameters();
		appendParameters(url, requestParameters.getParameters());
		return url;
	}
}