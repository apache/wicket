/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Simple choice list backed by an ArrayList. This class implements
 * {@link wicket.markup.html.form.model.IChoiceList}so that it is easier to
 * create subclasses and anonymous implementations.
 * 
 * @author Jonathan Locke
 */
public class ChoiceList implements IChoiceList
{
	/** List of choices */
	private final List list;

	/**
	 * Implementation of IChoice for simple objects.
	 * 
	 * @author Jonathan Locke
	 */
	private class Choice implements IChoice
	{
		/** The index of the choice */
		private final int index;

		/** The choice model object */
		private final Object object;

		/**
		 * Constructor
		 * 
		 * @param object
		 *            The object
		 * @param index
		 *            The index of the object in the choice list
		 */
		public Choice(final Object object, final int index)
		{
			this.object = object;
			this.index = index;
		}

		/**
		 * @see wicket.markup.html.form.model.IChoice#getDisplayValue()
		 */
		public String getDisplayValue()
		{
			return object.toString();
		}

		/**
		 * @see wicket.markup.html.form.model.IChoice#getId()
		 */
		public String getId()
		{
			return Integer.toString(index);
		}

		/**
		 * @see wicket.markup.html.form.model.IChoice#getObject()
		 */
		public Object getObject()
		{
			return object;
		}
	}

	/**
	 * Constructor
	 */
	public ChoiceList()
	{
		list = new ArrayList();
	}

	/**
	 * Constructor
	 * 
	 * @param choices
	 *            Choices to add to this list
	 */
	public ChoiceList(final Collection choices)
	{
		this();
		list.addAll(choices);
	}

	/**
	 * Constructor
	 * 
	 * @param initialCapacity
	 *            Initial capacity of list
	 */
	public ChoiceList(final int initialCapacity)
	{
		list = new ArrayList(initialCapacity);
	}

	/**
	 * @param object
	 *            Object to add to list
	 */
	public void add(final Object object)
	{
		attach();
		list.add(object);
	}

	/**
	 * Add all the elements from a collection to this choice list
	 * 
	 * @param collection
	 *            The collection
	 */
	public void addAll(final Collection collection)
	{
		attach();
		list.addAll(collection);
	}

	/**
	 * @see wicket.model.IDetachable#attach()
	 */
	public void attach()
	{
	}

	/**
	 * @see wicket.markup.html.form.model.IChoiceList#choiceForId(java.lang.String)
	 */
	public IChoice choiceForId(String id)
	{
		return get(Integer.parseInt(id));
	}

	/**
	 * @see wicket.markup.html.form.model.IChoiceList#choiceForObject(java.lang.Object)
	 */
	public IChoice choiceForObject(Object object)
	{
		return get(list.indexOf(object));
	}

	/**
	 * Clears this list
	 */
	public void clear()
	{
		list.clear();
	}

	/**
	 * @see wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
	}

	/**
	 * @see wicket.markup.html.form.model.IChoiceList#get(int)
	 */
	public IChoice get(final int index)
	{
		attach();
		if (index != -1)
		{
			return newChoice(list.get(index), index);
		}
		return null;
	}

	/**
	 * @return Returns the list.
	 */
	public List getList()
	{
		attach();
		return list;
	}

	/**
	 * @see wicket.markup.html.form.model.IChoiceList#size()
	 */
	public int size()
	{
		attach();
		return list.size();
	}

	/**
	 * IChoice factory method
	 * 
	 * @param object
	 *            Choice object
	 * @param index
	 *            Index of choice
	 * @return The IChoice wrapper for the object at the given index
	 */
	protected IChoice newChoice(final Object object, final int index)
	{
		return new Choice(list.get(index), index);
	}
}
