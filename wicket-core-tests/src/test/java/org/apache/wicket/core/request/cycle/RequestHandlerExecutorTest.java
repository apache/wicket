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
package org.apache.wicket.core.request.cycle;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.RequestHandlerExecutor;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.UrlRenderer;

/**
 * @author Jeremy Thomerson
 */
abstract class RequestHandlerExecutorTest
{
	Response newResponse()
	{
		return new Response()
		{
			@Override
			public void write(byte[] array)
			{
			}

			@Override
			public void write(byte[] array, int offset, int length)
			{
			}

			@Override
			public void write(CharSequence sequence)
			{
			}

			@Override
			public String encodeURL(CharSequence url)
			{
				return null;
			}

			@Override
			public Object getContainerResponse()
			{
				return null;
			}
		};
	}

	protected IRequestCycle newRequestCycle(Response response)
	{
		return new MockRequestCycle(response);
	}

	private class MockRequestCycle implements IRequestCycle
	{
		Response response;

		MockRequestCycle(Response response)
		{
			this.response = response;
		}

		@Override
		public Response getResponse()
		{
			return response;
		}

		@Override
		public Response setResponse(Response response)
		{
			Response original = this.response;
			this.response = response;
			return original;
		}

		@Override
		public Request getRequest()
		{
			return null;
		}

		@Override
		public void scheduleRequestHandlerAfterCurrent(IRequestHandler handler)
		{
		}

		@Override
		public UrlRenderer getUrlRenderer()
		{
			return null;
		}

	}

	protected RequestHandlerExecutor newStack(final IRequestCycle requestCycle)
	{
		return new RequestHandlerExecutor()
		{
			@Override
			protected void respond(IRequestHandler handler)
			{
				Response originalResponse = requestCycle.getResponse();
				try
				{
					handler.respond(requestCycle);
				}
				finally
				{
					requestCycle.setResponse(originalResponse);
				}
			}

			@Override
			protected void detach(IRequestHandler handler)
			{
				handler.detach(requestCycle);
			}

		};
	}
}
