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
package org.apache.wicket.atmosphere;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;

/**
 * Internal {@link IRequestMapper} to map {@link AtmosphereWebRequest} to
 * {@link AtmosphereRequestHandler}. This mapper is registered automatically by {@link EventBus}.
 * 
 * @author papegaaij
 */
public class AtmosphereRequestMapper implements IRequestMapper
{
	@Override
	public IRequestHandler mapRequest(Request request)
	{
		if (request instanceof AtmosphereWebRequest)
		{
			AtmosphereWebRequest pushRequest = (AtmosphereWebRequest)request;
			return new AtmosphereRequestHandler(pushRequest.getPageKey(),
				pushRequest.getSubscriptions(), pushRequest.getEvent());
		}
		return null;
	}

	@Override
	public int getCompatibilityScore(Request request)
	{
		return request instanceof AtmosphereWebRequest ? Integer.MAX_VALUE : 0;
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler)
	{
		return null;
	}
}
