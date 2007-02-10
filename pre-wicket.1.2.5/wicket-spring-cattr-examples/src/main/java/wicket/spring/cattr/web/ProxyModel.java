/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.spring.cattr.web;

import wicket.spring.common.Contact;
import wicket.spring.common.ContactDao;
import wicket.spring.common.web.ContactDetachableModel;

public class ProxyModel extends ContactDetachableModel {
	private ContactDao dao;

	public ProxyModel(Contact contact, ContactDao dao) {
		super(contact);
		this.dao = dao;
	}

	protected ContactDao getContactDao() {
		return dao;
	}

}
