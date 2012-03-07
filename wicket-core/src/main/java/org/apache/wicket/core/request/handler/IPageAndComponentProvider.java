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
	 * Returns the page relative component path.
	 *
	 * @return the page relative component path.
	 */
	String getComponentPath();

}