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

import org.apache.wicket.model.IDetachable;

public interface ManageablePage extends IDetachable
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


}
