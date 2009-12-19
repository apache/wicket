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
package org.apache.wicket.ng.page;

// TODO WICKET-NG: this iface used to extend IDetachable, however this causes problems because Page becomes IDetachable and some property models will cause an infinite loop trigging detach on the component again because it is their target object. In the future Component should indeed implement IDetachable, for now copied #detach into this iface directly.
public interface IManageablePage
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
}
