/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form.model;

import java.util.Collection;


/**
 * Adds attach/detach logic to ChoiceList. 
 * 
 * @author Johan Compagner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class DetachableChoiceList extends ChoiceList
{
	/**
	 * Transient flag to prevent multiple detach/attach scenario.
	 */
	private transient boolean attached = false;

	/**
	 * Construct.
	 */
	public DetachableChoiceList()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param initialCapacity
	 *            The initial capacity
	 */
	public DetachableChoiceList(final int initialCapacity)
	{
		super(initialCapacity);
	}

	/**
	 * Construct.
	 * 
	 * @param collection
	 *            a collection
	 */
	public DetachableChoiceList(Collection collection)
	{
		super(collection);
	}

	/**
	 * @see wicket.model.IDetachable#attach()
	 */
	public final void attach()
	{
		if (!attached)
		{
			attached = true;
			onAttach();
		}
	}

	/**
	 * @see wicket.model.IDetachable#detach()
	 */
	public final void detach()
	{
		if (attached)
		{
			attached = false;
			onDetach();
		}
	}

	/**
	 * Attach to the current request. Implement this method with custom
	 * behaviour, such as loading the list of object you need for this list.
	 */
	protected void onAttach()
	{
	}

	/**
	 * Detach from the current request. Implement this method with custom
	 * behaviour, such as clearing the list.
	 */
	protected void onDetach()
	{
		clear();
	}
}