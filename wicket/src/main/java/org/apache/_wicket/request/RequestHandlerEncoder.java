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

/**
 * Encodes {@link RequestHandler}(s) into {@link Url}(s) and decodes {@link Url}(s) to
 * {@link RequestHandler}(s). For {@link RequestHandler}s and {@link Url}s the implementation
 * doesn't know the {@link #encode(RequestHandler)} and {@link #decode(Url, RequestParameters)}
 * methods must return <code>null</code>.
 * 
 * @author Matej Knopp
 */
public interface RequestHandlerEncoder
{
	/**
	 * Returns {@link RequestHandler} for the request or <code>null</code> if the encoder does not
	 * recognize the URL.
	 * 
	 * @param url
	 * @param requestParameters
	 *            provides access to all GET and POST request parameters
	 * @return RequestHandler instance or <code>null</code>
	 */
	RequestHandler decode(Url url, RequestParameters requestParameters);

	/**
	 * Returns the {@link Url} for given {@link RequestHandler} or <code>null</code> if the
	 * encoder does not recognize the request handler.
	 * 
	 * @param requestHandler
	 * @return Url instance or <code>null</code>.
	 */
	Url encode(RequestHandler requestHandler);
}
