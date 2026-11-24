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
package org.apache.wicket.core.request.handler;

import org.apache.wicket.request.component.IRequestableComponent;

/**
 * Extension of {@link IPageProvider} that is also capable of providing a Component belonging to the
 * page.
 *
 * @author Matej Knopp
 */
public interface IPageAndComponentProvider extends IPageProvider
{
	/**
	 * Returns component on specified page with given path.
	 *
	 * @return component
	 */
	IRequestableComponent getComponent();

	/**
	 * Returns whether {@link #getComponent()} can return the component without creating the page
	 * or running any of its lifecycle callbacks, because the component has been resolved already
	 * or an existing page instance contains it.
	 * <p>
	 * Unlike {@link #getComponent()}, this method never initializes a stateless page or prepares it
	 * for rendering to let it add a missing component, so it can be called after the response has
	 * been written, e.g. to collect log data while the request handler is detached.
	 *
	 * @return {@code true} if {@link #getComponent()} returns the component without side effects
	 * @since 12.0.0
	 */
	boolean hasComponent();

	/**
	 * Returns the page relative component path.
	 *
	 * @return the page relative component path.
	 */
	String getComponentPath();

}