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
package org.apache.wicket.authorization.strategies.action;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.util.io.IClusterable;

/**
 * A way to provide authorization for a specific component action.
 * 
 * @author Jonathan Locke
 * @since 1.2
 */
public interface IActionAuthorizer extends IClusterable
{
	/**
	 * Gets the action that this authorizer authorizes.
	 * 
	 * @return The action that this authorizer authorizes
	 */
	Action getAction();

	/**
	 * Gets whether this action is authorized.
	 * 
	 * @param component
	 *            The component to authorize this action on
	 * @return True if this action is authorized
	 */
	boolean authorizeAction(Component component);
}
