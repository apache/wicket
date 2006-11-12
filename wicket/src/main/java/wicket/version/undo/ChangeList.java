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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;

/**
 * A ChangeList is a sequence of changes that can be undone.
 * 
 * @author Jonathan Locke
 */
class ChangeList implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** log. */
	private static final Log log = LogFactory.getLog(ChangeList.class);

	/** the changes. */
	private List<Change> changes = new ArrayList<Change>();

	/**
	 * A component was added.
	 * 
	 * @param component
	 *            the added component
	 */
	void componentAdded(Component component)
	{
		changes.add(new Add(component));
	}

	/**
	 * A model is about to change.
	 * 
	 * @param <T>
	 *            The type
	 * @param component
	 *            the component of which the model changed
	 */
	<T> void componentModelChanging(Component<T> component)
	{
		changes.add(new ModelChange<T>(component));
	}

	/**
	 * The state of a component is about to change.
	 * 
	 * @param change
	 *            the change object
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
	 * A component was removed from its parent.
	 * 
	 * @param component
	 *            the component that was removed
	 */
	void componentRemoved(Component component)
	{
		changes.add(new Remove(component));
	}

	/**
	 * Undo changes (rollback).
	 */
	void undo()
	{
		// Go through changes in reverse time order to undo
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
