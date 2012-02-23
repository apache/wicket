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
package org.apache.wicket.request.mapper.parameter;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;

/**
 * Encoder that can encode and decode {@link PageParameters} to/from URL.
 * 
 * @author Matej Knopp
 */
public interface IPageParametersEncoder
{
	/**
	 * Encode the given {@link PageParameters} instance into URL. The URL will be then merged with
	 * the URL generated for {@link IRequestHandler}.
	 * 
	 * @param pageParameters
	 *            parameters to encode
	 * @return Url generated from the page parameters
	 */
	Url encodePageParameters(PageParameters pageParameters);

	/**
	 * Decodes the given URL to {@link PageParameters}. The URL will have all
	 * {@link IRequestHandler} specified segments/parameters stripped.
	 * 
	 * @param url
	 *            url to decode
	 * @return {@link PageParameters} instance or <code>null</code> if empty
	 */
	PageParameters decodePageParameters(Url url);
}
