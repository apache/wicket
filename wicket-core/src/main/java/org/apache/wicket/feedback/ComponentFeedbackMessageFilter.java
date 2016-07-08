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
package org.apache.wicket.feedback;

import org.apache.wicket.Component;
import org.apache.wicket.util.lang.Objects;

/**
 * Filter for accepting feedback messages for a particular component.
 * 
 * @author Jonathan Locke
 */
public class ComponentFeedbackMessageFilter implements IFeedbackMessageFilter
{
	private static final long serialVersionUID = 1L;

	/** The component to accept feedback messages for */
	private final Component component;

	/**
	 * Constructor
	 * 
	 * @param component
	 *            The component to filter on
	 */
	public ComponentFeedbackMessageFilter(Component component)
	{
		this.component = component;
	}

	@Override
	public boolean accept(FeedbackMessage message)
	{
		return Objects.equal(component, message.getReporter());
	}
}
