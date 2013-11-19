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
package org.apache.wicket.cdi;

import org.apache.wicket.request.IRequestHandler;

/**
 * A strategy that specifies how conversations should be propagated between pages/resources.
 * {@link ConversationPropagation} provides sensible default implementations of this interface.
 * 
 * @author papegaaij
 */
public interface IConversationPropagation
{
	/**
	 * Indicates if the conversation should be propagated via url-parameters for the given request
	 * handler. This can either be a get parameter in a rendered url, or via page parameters.
	 * 
	 * @param handler
	 *            The current request handler
	 * @return true if the conversation should be propagated for the given request handler.
	 */
	public boolean propagatesViaParameters(IRequestHandler handler);
}
