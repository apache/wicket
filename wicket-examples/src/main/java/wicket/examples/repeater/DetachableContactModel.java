/*
 * $Id: DetachableContactModel.java 5832 2006-05-24 05:36:28 +0000 (Wed, 24 May
 * 2006) ivaynberg $ $Revision$ $Date: 2006-05-24 05:36:28 +0000 (Wed, 24
 * May 2006) $
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

import wicket.markup.repeater.IItemReuseStrategy;
import wicket.model.AbstractReadOnlyDetachableModel;

/**
 * detachable model for an instance of contact
 * 
 * @author igor
 * 
 */
public class DetachableContactModel extends AbstractReadOnlyDetachableModel<Contact>
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

	@Override
	protected void onAttach()
	{
		if (contact == null)
		{
			contact = getContactsDB().get(id);
		}
	}

	@Override
	protected void onDetach()
	{
		contact = null;
	}

	@Override
	protected Contact onGetObject()
	{
		return contact;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return new Long(id).hashCode();
	}

	/**
	 * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
	 * 
	 * @see wicket.markup.repeater.AbstractPageableView#setItemReuseStrategy(IItemReuseStrategy)
	 * @see wicket.markup.repeater.ReuseIfModelsEqualStrategy
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
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
