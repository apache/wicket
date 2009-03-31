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
package org.apache.wicket.version.undo;

import org.apache.wicket.Component;
import org.apache.wicket.util.lang.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An add change operation.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
class Add extends Change
{
	private static final long serialVersionUID = 1L;

	/** logger */
	private static final Logger log = LoggerFactory.getLogger(Add.class);

	/** the subject <code>Component</code> */
	private final Component component;

	/**
	 * Constructor.
	 * 
	 * @param component
	 *            the subject <code>Component</code>
	 */
	Add(final Component component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("argument component must be not null");
		}

		if (component.getParent() == null)
		{
			throw new IllegalStateException("component " + component + " doesn't have a parent");
		}

		if (log.isDebugEnabled())
		{
			log.debug("RECORD ADD: added " + component.getPath() + " (" +
				Classes.simpleName(component.getClass()) + "@" + component.hashCode() +
				") to parent");
		}

		this.component = component;
	}

	/**
	 * @see Change#undo()
	 */
	@Override
	public void undo()
	{
		if (log.isDebugEnabled())
		{
			log.debug("UNDO ADD: removing " + component.getPath() + " (" +
				Classes.simpleName(component.getClass()) + "@" + component.hashCode() +
				") from parent");
		}

		// Check if somehow the Component isn't already removed.
		if (component.getParent() != null)
		{
			component.remove();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Add[component: " + component.getPath() + "]";
	}
}
