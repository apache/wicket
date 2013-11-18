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

import javax.enterprise.context.ConversationScoped;

import org.apache.wicket.core.request.handler.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.request.IRequestHandler;

/**
 * Various modes of propagating persistent conversations across requests.
 * 
 * @see ConversationScoped
 * 
 * @author igor
 */
public enum ConversationPropagation implements IConversationPropagation {
	/** No conversational propagation takes place */
	NONE {
		@Override
		public boolean propagatesViaParameters(IRequestHandler handler)
		{
			return false;
		}
	},
	/**
	 * Pesistent conversations are propagated between non-bookmarkable pages
	 * only
	 * 
	 * @deprecated as of cdi-1.1, it is specified that conversations are
	 *             propagated via the cid query parameter even for
	 *             non-bookmarkable pages
	 */
	@Deprecated
	NONBOOKMARKABLE {
		@Override
		public boolean propagatesViaParameters(IRequestHandler handler)
		{
			return !(handler instanceof BookmarkableListenerInterfaceRequestHandler)
					&& !(handler instanceof BookmarkablePageRequestHandler);
		}
	},
	/**
	 * Persistent conversations are propagated between bookmarkable and
	 * non-bookmarkable pages
	 */
	ALL {
		@Override
		public boolean propagatesViaParameters(IRequestHandler handler)
		{
			return true;
		}
	};
}
