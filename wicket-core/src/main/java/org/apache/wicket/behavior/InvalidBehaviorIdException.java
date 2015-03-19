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
package org.apache.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;

/**
 * Thrown when a behavior with an invalid id is requested
 * 
 * @author ivaynberg (Igor Vaynberg)
 */
public class InvalidBehaviorIdException extends WicketRuntimeException
{
	private static final long serialVersionUID = 1L;

	private final Component component;
	private final int behaviorId;

	/**
	 * Constructor
	 * 
	 * @param component
	 * @param behaviorId
	 */
	public InvalidBehaviorIdException(Component component, int behaviorId)
	{
		super(
			String.format("Cannot find behavior with id '%d' on component '%s' in page '%s'. " +
				"Perhaps the behavior did not properly implement getStatelessHint() and returned 'true' " +
				"to indicate that it is stateless instead of returning 'false' to indicate that it is stateful.",
					behaviorId,
					component.getClassRelativePath(),
					component.getPage()));
		this.component = component;
		this.behaviorId = behaviorId;
	}

	/**
	 * @return component which was thought to contain the behavior
	 */
	public Component getComponent()
	{
		return component;
	}

	/**
	 * @return behavior id
	 */
	public int getBehaviorId()
	{
		return behaviorId;
	}
}
