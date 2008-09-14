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
package org.apache._wicket.request;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache._wicket.request.request.Request;

/**
 * Thread safe registry of {@link RequestHandlerEncoder}s. The encoders are searched depending on
 * the orders they were registered. Last registered encoder has the highest priority.
 * 
 * @author Matej Knopp
 */
public class RequestHandlerEncoderRegistry
{
	/**
	 * Registers a {@link RequestHandlerEncoder}
	 * 
	 * @param encoder
	 */
	public void register(RequestHandlerEncoder encoder)
	{
		encoders.add(0, encoder);
	}

	/**
	 * Unregisters {@link RequestHandlerEncoder}
	 * 
	 * @param encoder
	 */
	public void unregister(RequestHandlerEncoder encoder)
	{
		encoders.remove(encoder);
	}

	/**
	 * Searches the registered {@link RequestHandlerEncoder}s to find one that can decode the
	 * {@link Request}. Each registered {@link RequestHandlerEncoder} is asked to decode the
	 * {@link Request} until an encoder that can decode the {@link Request} is found or no more
	 * encoders are left.
	 * <p>
	 * The handlers are searched in reverse order as they have been registered. More recently
	 * registered handlers have bigger priority.
	 * 
	 * @param request
	 * @return RequestHandler for the request or <code>null</code> if no encoder for the request
	 *         is found.
	 */
	public RequestHandler decode(Request request)
	{
		for (RequestHandlerEncoder encoder : encoders)
		{
			RequestHandler handler = encoder.decode(request);
			if (handler != null)
			{
				return handler;
			}
		}
		return null;
	}

	/**
	 * Searchers the registered {@link RequestHandlerEncoder}s to find one that can encode the
	 * {@link RequestHandler}. Each registered {@link RequestHandlerEncoder} is asked to encode the
	 * {@link RequestHandler} until an encoder that can encode the {@link RequestHandler} is found
	 * or no more encoders are left.
	 * <p>
	 * The handlers are searched in reverse order as they have been registered. More recently
	 * registered handlers have bigger priority.
	 * 
	 * @param handler
	 * @return Url for the handler or <code>null</code> if no encoder for the handler is found.
	 */
	public Url encode(RequestHandler handler)
	{
		for (RequestHandlerEncoder encoder : encoders)
		{
			Url url = encoder.encode(handler);
			if (url != null)
			{
				return url;
			}
		}
		return null;
	}

	private List<RequestHandlerEncoder> encoders = new CopyOnWriteArrayList<RequestHandlerEncoder>();

}
