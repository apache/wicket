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
package org.apache.wicket.devutils.stateless;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;

/**
 *
 * Just an exception that can be thrown if a StatelessChecker is invoked, the component being checked is not stateless
 * or the behavior held by the component is not stateless.
 *
 * Includes a  method that to get check failure component.
 *
 * @author Ken Sakurai
 */
public class StatelessCheckFailureException  extends WicketRuntimeException
{
    private static final long serialVersionUID = 1L;

    private Component component;

	/**
	 * Construct.
	 * @param component Failure component
	 * @param reason Reason for exception occurrence
	 */
	public StatelessCheckFailureException(Component component, String reason)
    {
        super("'" + component + "' claims to be stateless but isn't. ");
        this.component = component;
    }

	/**
	 * Get check failure component
	 * @return Failure component
	 */
	public Component getComponent() {
		return component;
	}
}
