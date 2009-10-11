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
package org.apache.wicket.ng.request.mapper;

import org.apache.wicket.ng.request.Request;
import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.RequestMapper;
import org.apache.wicket.ng.request.Url;

/**
 * Mapper that encapsulates mappers that are necessary for Wicket to function.
 * 
 * @author igor.vaynberg
 * 
 */
public class SystemMapper implements RequestMapper
{
	private final ThreadsafeCompoundRequestMapper mapper = new ThreadsafeCompoundRequestMapper();

	/**
	 * Constructor
	 */
	public SystemMapper()
	{
		mapper.register(new PageInstanceMapper());
		mapper.register(new BookmarkableMapper());
		mapper.register(new ResourceReferenceMapper());
		mapper.register(new BufferedResponseMapper());
	}

	/** {@inheritDoc} */
	public int getCompatibilityScore(Request request)
	{
		return mapper.getCompatibilityScore(request);
	}

	/** {@inheritDoc} */
	public Url mapHandler(RequestHandler handler)
	{
		return mapper.mapHandler(handler);
	}

	/** {@inheritDoc} */
	public RequestHandler mapRequest(Request request)
	{
		return mapper.mapRequest(request);
	}


}
