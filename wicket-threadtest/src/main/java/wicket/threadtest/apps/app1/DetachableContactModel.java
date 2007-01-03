/*
 * $Id: DetachableContactModel.java 5394 2006-04-16 06:36:52 -0700 (Sun, 16 Apr 2006) jdonnerstag $
 * $Revision: 5394 $ $Date: 2006-04-16 06:36:52 -0700 (Sun, 16 Apr 2006) $
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
package wicket.threadtest.apps.app1;

import wicket.markup.repeater.IItemReuseStrategy;
import wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import wicket.model.AbstractReadOnlyDetachableModel;

/**
 * detachable model for an instance of contact
 * 
 * @author igor
 * 
 */
public class DetachableContactModel extends AbstractReadOnlyDetachableModel<Contact> {
	private transient Contact contact;

	private long id;

	/**
	 * @param c
	 */
	public DetachableContactModel(Contact c) {
		this(c.getId());
		contact = c;
	}

	/**
	 * @param id
	 */
	public DetachableContactModel(long id) {
		if (id == 0) {
			throw new IllegalArgumentException();
		}
		this.id = id;
	}

	/**
	 * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
	 * 
	 * @see AbstractPageableView#setItemReuseStrategy(IItemReuseStrategy)
	 * @see ReuseIfModelsEqualStrategy
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DetachableContactModel) {
			DetachableContactModel other = (DetachableContactModel) obj;
			return other.id == this.id;
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new Long(id).hashCode();
	}

	protected ContactsDatabase getContactsDB() {
		return DatabaseLocator.getDatabase();
	}

	@Override
	protected void onAttach() {
		if (contact == null) {
			contact = getContactsDB().get(id);
		}
	}

	@Override
	protected void onDetach() {
		contact = null;
	}

	@Override
	protected Contact onGetObject() {
		return contact;
	}
}