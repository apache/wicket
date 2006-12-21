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
package wicket.request.target.coding;

import wicket.IRequestTarget;
import wicket.request.RequestParameters;

/**
 * Implementations of this interface know how to encode and decode request
 * targets to/from a URL.
 * 
 * @author Eelco Hillenius
 */
public interface IRequestTargetUrlCodingStrategy
{
	/**
	 * Gets the decoded request target.
	 * 
	 * @param requestParameters
	 *            the request parameters
	 * @return the decoded request target
	 */
	IRequestTarget decode(RequestParameters requestParameters);

	/**
	 * Gets the encoded url for the provided request target. Typically, the
	 * result will be prepended with a protocol specific prefix. In a servlet
	 * environment, the prefix typically is the context-path + servlet path, eg
	 * mywebapp/myservletname.
	 * 
	 * @param requestTarget
	 *            the request target to encode
	 * 
	 * @return the encoded url
	 */
	CharSequence encode(IRequestTarget requestTarget);

	/**
	 * Gets whether this mounter is applicable for the provided request target.
	 * 
	 * @param requestTarget
	 *            the request target
	 * @return whether this mounter is applicable for the provided request
	 *         target
	 */
	boolean matches(IRequestTarget requestTarget);
}