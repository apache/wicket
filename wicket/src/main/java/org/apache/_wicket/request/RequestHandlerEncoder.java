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

import org.apache._wicket.request.request.Request;

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
	 * @param request
	 *            provides access to request data (i.e. Url and Parameters)
	 *            
	 * @return RequestHandler instance or <code>null</code>
	 */
	RequestHandler decode(Request request);
	
	/**
	 * Returns the amount of matching segments for the request. When two {@link RequestHandlerEncoder}s 
	 * are capable of decoding a request, the one with highest maching segments count will be used.
	 * <p>
	 * For example when there are two encoders for mounted page, one mapped to <code>/foo</code> another
	 * to <code>/foo/bar</code> and the incomming reqest URL is </code>/foo/bar/baz</code>, the encoder
	 * mapped to <code>/foo/bar</code> will handle the request as it has matching segments count of 2
	 * while the first one has only matching segments count of 1.
	 * <p>
	 * Note that the method can return value &gt; 0 even if the encoder can not decode the request. 
	 * 
	 * @param request
	 * @return count of matching segments
	 */
	public int getMachingSegmentsCount(Request request);

	/**
	 * Returns the {@link Url} for given {@link RequestHandler} or <code>null</code> if the
	 * encoder does not recognize the request handler.
	 * 
	 * @param requestHandler
	 * @return Url instance or <code>null</code>.
	 */
	Url encode(RequestHandler requestHandler);
}
