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
package org.apache.wicket.request.mapper.mount;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;

/**
 * TODO javadoc, explain "parameters resolved from the mount"
 * 
 * @author igor.vaynberg
 * @deprecated Will be removed in Wicket 8.0
 */
@Deprecated
public interface IMountedRequestMapper
{
	/**
	 * Returns {@link org.apache.wicket.request.IRequestHandler} for the request or <code>null</code> if the encoder does not
	 * recognize the URL.
	 *
	 * @param request
	 *            provides access to request data (i.e. Url and Parameters)
	 * @param mountParams
	 *            parameters resolved from the mount
	 * @return RequestHandler instance or <code>null</code>
	 */
	IRequestHandler mapRequest(Request request, MountParameters mountParams);

	/**
	 * Returns the score representing how compatible this request mapper is to processing the given
	 * request. When a request comes in all mappers are scored and are tried in order from highest
	 * score to lowest.
	 * <p>
	 * A good criteria for calculating the score is the number of matched url segments. For example
	 * when there are two encoders for mounted page, one mapped to <code>/foo</code> another to
	 * <code>/foo/bar</code> and the incomming reqest URL is </code>/foo/bar/baz</code>, the encoder
	 * mapped to <code>/foo/bar</code> will handle the request first as it has matching segments
	 * count of 2 while the first one has only matching segments count of 1.
	 * <p>
	 * Note that the method can return value greater then zero even if the encoder can not decode
	 * the request.
	 *
	 * @param request
	 * @return count of matching segments
	 */
	int getCompatibilityScore(Request request);

	/**
	 * Returns the {@link Mount} for given {@link org.apache.wicket.request.IRequestHandler} or <code>null</code> if the
	 * encoder does not recognize the request handler.
	 * 
	 * @param requestHandler
	 * @return Url instance or <code>null</code>.
	 */
	Mount mapHandler(IRequestHandler requestHandler);
}
