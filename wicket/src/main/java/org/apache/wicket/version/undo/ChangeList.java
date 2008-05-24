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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>ChangeList</code> is a sequence of changes that can be undone.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
class ChangeList implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/** logger */
	private static final Logger log = LoggerFactory.getLogger(ChangeList.class);

	/** the list of changes */
	private final List<Change> changes = new ArrayList<Change>();

	/**
	 * A <code>Component</code> was added.
	 * 
	 * @param component
	 *            the <code>Component</code> that was added
	 */
	void componentAdded(Component<?> component)
	{
		changes.add(new Add(component));
	}

	/**
	 * An <code>IModel</code> is about to change.
	 * 
	 * @param <T>
	 *            type of component's model object
	 * 
	 * @param component
	 *            the <code>Component</code> for which the <code>IModel</code> changed
	 */
	<T> void componentModelChanging(Component<T> component)
	{
		changes.add(new ModelChange<T>(component));
	}

	/**
	 * The state of a <code>Component</code> is about to change.
	 * 
	 * @param change
	 *            the <code>Change</code> object
	 */
	void componentStateChanging(Change change)
	{
		if (log.isDebugEnabled())
		{
			log.debug("RECORD CHANGE: " + change);
		}

		changes.add(change);
	}

	/**
	 * A <code>Component</code> was removed from its parent.
	 * 
	 * @param component
	 *            the <code>Component</code> that was removed
	 */
	void componentRemoved(Component<?> component)
	{
		changes.add(new Remove(component));
	}

	/**
	 * Adds the given <code>ChangeList</code> to this <code>ChangeList</code>.
	 * 
	 * @param list
	 *            the <code>ChangeList</code> to add
	 */
	void add(ChangeList list)
	{
		changes.addAll(list.changes);
	}

	/**
	 * Undo changes (roll back).
	 */
	void undo()
	{
		// Go through changes in reverse time order to undo.
		for (int i = changes.size() - 1; i >= 0; i--)
		{
			changes.get(i).undo();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return changes.toString();
	}

}
