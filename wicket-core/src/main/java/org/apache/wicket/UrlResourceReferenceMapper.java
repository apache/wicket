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
package org.apache.wicket;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.resource.bundles.ResourceBundleReference;

/**
 * A request mapper that is used to create Url out of UrlResourceReference.
 * UrlResourceReference works with {@link org.apache.wicket.request.resource.UrlResourceReference.CalculatedUrl} and
 * thus this mapper should not use {@link org.apache.wicket.SystemMapper.ParentFolderPlaceholderProvider}
 */
public class UrlResourceReferenceMapper implements IRequestMapper
{
	@Override
	public Url mapHandler(IRequestHandler requestHandler)
	{
		Url url = null;
		if (requestHandler instanceof ResourceReferenceRequestHandler)
		{
			ResourceReferenceRequestHandler resourceReferenceRequestHandler = (ResourceReferenceRequestHandler) requestHandler;
			ResourceReference resourceReference = resourceReferenceRequestHandler.getResourceReference();

			while (resourceReference instanceof ResourceBundleReference)
			{
				// unwrap the bundle to render the url for the actual reference
				resourceReference = ((ResourceBundleReference)resourceReference).getBundleReference();
			}

			if (resourceReference instanceof UrlResourceReference)
			{
				UrlResourceReference urlResourceReference = (UrlResourceReference) resourceReference;
				url = urlResourceReference.getUrl();
			}
		}
		return url;
	}

	@Override
	public IRequestHandler mapRequest(Request request)
	{
		return null;
	}

	@Override
	public int getCompatibilityScore(Request request)
	{
		return Integer.MIN_VALUE;
	}

}
