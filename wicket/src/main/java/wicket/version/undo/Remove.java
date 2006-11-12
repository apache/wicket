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
package wicket.version.undo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.util.lang.Classes;

/**
 * A remove change operation.
 * 
 * @author Jonathan Locke
 */
class Remove extends Change
{
	private static final long serialVersionUID = 1L;

	/** log. */
	private static final Log log = LogFactory.getLog(Remove.class);

	/** subject. */
	private final Component component;

	/** parent. */
	private final MarkupContainer container;

	/**
	 * Construct.
	 * 
	 * @param component
	 *            subject component
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
	 * @see wicket.version.undo.Change#undo()
	 */
	@Override
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
	@Override
	public String toString()
	{
		return "Remove[component: " + component.getPath() + "]";
	}

}
