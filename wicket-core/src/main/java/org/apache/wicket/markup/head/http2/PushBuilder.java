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
package org.apache.wicket.markup.head.http2;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.markup.head.http2.PushItemHeaderValue.HeaderOperation;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to push resources with the Servlet 4 specific push builder API
 * 
 * @author Tobias Soloschenko
 */
public class PushBuilder implements IPushBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(PushBuilder.class);

	@Override
	public void push(HttpServletRequest httpServletRequest, PushItem... pushItems)
	{
		Request request = RequestCycle.get().getRequest();
		HttpServletRequest httpRequest = (HttpServletRequest) request.getContainerRequest();
		jakarta.servlet.http.PushBuilder pushBuilder = httpRequest.newPushBuilder();
		if (pushBuilder != null)
		{
			for (PushItem pushItem : pushItems)
			{
				pushBuilder.path(pushItem.getUrl());
				pushItem.getHeaders().entrySet().stream().forEach(pushHeader -> {
					String key = pushHeader.getKey();
					PushItemHeaderValue value = pushHeader.getValue();
					if(value.getOperation() == HeaderOperation.ADD){
						pushBuilder.addHeader(key, value.getValue());
					}else{
						pushBuilder.setHeader(key, value.getValue());
					}
				});
				pushBuilder.push();	
			}
		}
		else
		{
			LOG.warn("Attempted to use HTTP2 Push but it is not supported for the current request: {}!",
					httpRequest);
		}
	}
}
