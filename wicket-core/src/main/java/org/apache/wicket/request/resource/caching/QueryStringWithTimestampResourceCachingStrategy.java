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
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Time;

/**
 * resource caching strategy that adds a last-modified timestamp to the query string of the resource
 * (this is similar to how wicket 1.4 does it when enabling timestamps on resources).
 * 
 * @author Peter Ertl
 */
public class QueryStringWithTimestampResourceCachingStrategy extends
	AbstractResourceCachingStrategy
{
	private static final String DEFAULT_TIMESTAMP_PARAMETER = "ts";

	private final String timestampParameter;

	/**
	 * Constructor
	 */
	public QueryStringWithTimestampResourceCachingStrategy()
	{
		this(DEFAULT_TIMESTAMP_PARAMETER);
	}

	/**
	 * Constructor
	 * 
	 * @param timestampParameter
	 *            name of timestamp parameter which will be added to query string
	 */
	public QueryStringWithTimestampResourceCachingStrategy(String timestampParameter)
	{
		Args.notEmpty(timestampParameter, "timestampParameter");
		this.timestampParameter = timestampParameter;
	}

	/**
	 * @return name of timestamp parameter which will be added to query string
	 */
	public final String getTimestampParameter()
	{
		return timestampParameter;
	}

	public void decorateUrl(ResourceUrl url, final ResourceReference reference)
	{
		Time lastModified = getLastModified(reference);

		if (lastModified != null)
		{
			url.getParameters().set(timestampParameter, lastModified.getMilliseconds());
		}
	}

	public void undecorateUrl(ResourceUrl url)
	{
		url.getParameters().remove(timestampParameter);
	}

	public void decorateResponse(AbstractResource.ResourceResponse response)
	{
		response.setCacheDurationToMaximum();
		response.setCacheScope(WebResponse.CacheScope.PUBLIC);
	}
}
