/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.repeater;

import wicket.Component;
import wicket.model.AbstractReadOnlyDetachableModel;
import wicket.model.IModel;

/**
 * detachable model for an instance of contact
 * 
 * @author igor
 * 
 */
public class DetachableContactModel extends AbstractReadOnlyDetachableModel
{
	private long id;
	private transient Contact contact;

	protected ContactsDatabase getContactsDB()
	{
		return DatabaseLocator.getDatabase();
	}

	/**
	 * @param c
	 */
	public DetachableContactModel(Contact c)
	{
		this(c.getId());
		contact = c;
	}

	/**
	 * @param id
	 */
	public DetachableContactModel(long id)
	{
		if (id == 0)
		{
			throw new IllegalArgumentException();
		}
		this.id = id;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return null;
	}

	protected void onAttach()
	{
		if (contact == null)
		{
			contact = getContactsDB().get(id);
		}
	}

	protected void onDetach()
	{
		contact = null;
	}

	protected Object onGetObject(Component component)
	{
		return contact;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return new Long(id).hashCode();
	}

	/**
	 * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof DetachableContactModel)
		{
			DetachableContactModel other = (DetachableContactModel)obj;
			return other.id == this.id;
		}
		return false;
	}
}
