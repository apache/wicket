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
package org.apache.wicket.page;

import org.apache.wicket.util.io.IClusterable;

// TODO WICKET-NG: this iface used to extend IDetachable, however this causes problems because Page becomes IDetachable and some property models will cause an infinite loop triggering detach on the component again because it is their target object. In the future Component should indeed implement IDetachable, for now copied #detach into this iface directly.
/**
 * TODO javadoc
 */
public interface IManageablePage extends IClusterable
{
	/**
	 * Gets whether the page is stateless. Components on stateless page must not render any stateful
	 * urls. Stateful urls are urls, which refer to a certain (current) page instance and don't
	 * contain enough information to reconstruct page if it's not available (page class).
	 * 
	 * @return Whether this page is stateless
	 */
	// note that this has different semantics than Component#isStateless()
	public boolean isPageStateless();

	/**
	 * @return A unique identifier for this page map entry
	 */
	public int getPageId();

	/**
	 * Detaches model after use. This is generally used to null out transient references that can be
	 * re-attached later.
	 */
	void detach();

	/**
	 * Sets whether or not the page is allowed to change its page id. Implementations of this
	 * interface usually change their page id once a change to the data structure is made and
	 * historical record of the current state needs to be kept (usually to be accessible via the
	 * back button). Keeping a historical record is usually achieved by simply incrementing the page
	 * id to the next unique number, so when the implementation is stored it is done so in a new
	 * slot.
	 * 
	 * This method is useful when for some reason we do not want the implementation to change its
	 * page id under any circumstances. One concrete example is an AJAX request. Suppose the page
	 * with id 10 was written out with callbacks pointing to id 10. Suppose that the user executed
	 * some AJAX callbacks which have changed the page id to 15. Now, the user clicks a non-AJAX
	 * link that was never updated by an AJAX update and still points to id 10 - which causes the
	 * state of the page to be rolled back - which is usually undesirable as all changes made to the
	 * page by AJAX requests are lost. So, instead, whatever is invoking the execution of the AJAX
	 * request on the page can use this method to tell the page to not update its page id thereby
	 * solving the problem.
	 * 
	 * @param freeze
	 * 
	 * @return previous state
	 */
	boolean setFreezePageId(boolean freeze);
}
