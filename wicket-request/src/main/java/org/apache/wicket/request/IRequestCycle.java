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
package org.apache.wicket.request;

/**
 * TODO javadoc
 */
public interface IRequestCycle
{
	/**
	 * Get the active response at the request cycle.
	 * 
	 * @return response
	 */
	Response getResponse();

	/**
	 * Replaces the current {@link Response} with new {@link Response} instance. The original response
	 * is always restored after the {@link IRequestHandler#respond(IRequestCycle)} method is
	 * finished.
	 * 
	 * @param response
	 * @return Response being replaced.
	 */
	Response setResponse(Response response);

	/**
	 * TODO Wicket 7
	 * Add the following method to the API:
	 *
	 * Replaces the current {@link Request} with a new one.
	 * @return the previous request
	 */
//	Request setRequest(Request request);

	/**
	 * @return the request that originated this cycle
	 */
	Request getRequest();

	/**
	 * Schedule the request handler to be executed after the current one.
	 * 
	 * @param handler
	 */
	void scheduleRequestHandlerAfterCurrent(IRequestHandler handler);

	/**
	 * Returns {@link UrlRenderer} for this {@link IRequestCycle}.
	 * 
	 * @return UrlRenderer instance.
	 */
	UrlRenderer getUrlRenderer();
}
