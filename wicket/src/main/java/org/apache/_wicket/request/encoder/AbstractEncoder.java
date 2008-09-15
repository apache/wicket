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
package org.apache._wicket.request.encoder;

import org.apache._wicket.request.RequestHandler;
import org.apache._wicket.request.RequestHandlerEncoder;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.request.Request;
import org.apache.wicket.RequestListenerInterface;

public abstract class AbstractEncoder implements RequestHandlerEncoder
{
	protected EncoderContext getContext()
	{
		return null;
	};

	protected String requestListenerInterfaceToString(RequestListenerInterface listenerInterface)
	{
		if (listenerInterface == null)
		{
			throw new IllegalArgumentException("Argument 'listenerInterface' may not be null.");
		}
		return listenerInterface.getName();
	}
	
	protected RequestListenerInterface requestListenerIntefaceFromString(String interfaceName)
	{
		if (interfaceName == null)
		{
			throw new IllegalArgumentException("Argument 'interfaceName' may not be null.");
		}
		return RequestListenerInterface.forName(interfaceName);
	}
	
	public RequestHandler decode(Request request)
	{
		return null;
	}

	public Url encode(RequestHandler requestHandler)
	{
		return null;
	}

	public int getMachingSegmentsCount(Request request)
	{
		return 0;
	}

}
