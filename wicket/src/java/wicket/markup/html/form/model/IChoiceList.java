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

import wicket.model.IDetachable;

/**
 * A List interface that is used by the choice implementations to access a
 * detachable list of choices by index and id.
 * 
 * @author Johan Compagner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public interface IChoiceList extends IDetachable
{
	/**
	 * Attaches model for use. This is generally used to fill in transient
	 * fields in a model which has been serialized during session replication.
	 */
	public void attach();

	/**
	 * Gets an choice by id
	 * 
	 * @param id
	 *            The choice's id
	 * @return The choice
	 */
	public IChoice choiceForId(String id);

	/**
	 * @param object
	 *            The object to find
	 * @return The choice for the object
	 */
	public IChoice choiceForObject(Object object);

	/**
	 * @param index
	 *            The index of the choice to get
	 * @return The choice
	 */
	public IChoice get(int index);

	/**
	 * @return Number of choices in the list
	 */
	public int size();
}
