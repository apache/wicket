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
package org.apache.wicket.request.resource.caching;

import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Time;

/**
 * resource caching strategy that adds a last-modified
 * timestamp to the query string of the resource (this is
 * similar to how wicket 1.4 does it when enabling timestamps
 * on resources).
 *
 * @author Peter Ertl
 */
public class QueryStringWithTimestampResourceCachingStrategy extends AbstractResourceCachingStrategy
{
	private static final String DEFAULT_TIMESTAMP_PARAMETER = "ts";

	private String timestampParameter;

	public QueryStringWithTimestampResourceCachingStrategy()
	{
		timestampParameter = DEFAULT_TIMESTAMP_PARAMETER;
	}

	public String getTimestampParameter()
	{
		return timestampParameter;
	}

	public void setTimestampParameter(String timestampParameter)
	{
		Args.notEmpty(timestampParameter, "timestampParameter");
		this.timestampParameter = timestampParameter;
	}

	public String decorateRequest(final String filename, final PageParameters parameters,
		final ResourceReference reference)
	{
		Time lastModified = getLastModified(reference);

		if (lastModified != null)
		{
			parameters.add(timestampParameter, lastModified.getMilliseconds());
		}
		return filename;
	}

	public String sanitizeRequest(final String filename, PageParameters parameters)
	{
		parameters.remove(timestampParameter);
		return filename;
	}

	/**
	 * set resource caching to maximum and set cache-visibility to 'public'
	 *
	 * @param response
	 */
	public void processResponse(AbstractResource.ResourceResponse response)
	{
		response.setCacheDurationToMaximum();
		response.setCacheScope(WebResponse.CacheScope.PUBLIC);
	}
}
