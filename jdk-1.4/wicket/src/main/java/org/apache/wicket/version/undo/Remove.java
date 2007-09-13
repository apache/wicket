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
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.util.lang.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A remove change operation.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
class Remove extends Change
{
	private static final long serialVersionUID = 1L;

	/** logger */
	private static final Logger log = LoggerFactory.getLogger(Remove.class);

	/** the subject <code>Component</code> */
	private final Component component;

	/** the parent <code>MarkupContainer</code> */
	private final MarkupContainer container;

	/**
	 * Constructor.
	 * 
	 * @param component
	 *            the subject <code>Component</code>
	 */
	Remove(final Component component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("argument component must be not null");
		}

		this.component = component;
		this.container = component.getParent();

		if (this.container == null)
		{
			throw new IllegalArgumentException("component must have a parent");
		}

		if (log.isDebugEnabled())
		{
			log.debug("RECORD REMOVE: removed " + component.getPath() + " ("
					+ Classes.simpleName(component.getClass()) + "@" + component.hashCode()
					+ ") from parent");
		}
	}

	/**
	 * @see Change#undo()
	 */
	public void undo()
	{
		if (log.isDebugEnabled())
		{
			log.debug("UNDO REMOVE: re-adding " + component.getPath() + " ("
					+ Classes.simpleName(component.getClass()) + "@" + component.hashCode()
					+ ") to parent");
		}

		container.internalAdd(component);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "Remove[component: " + component.getPath() + "]";
	}

}
