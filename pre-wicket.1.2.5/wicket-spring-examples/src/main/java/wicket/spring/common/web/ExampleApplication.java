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
package wicket.spring.common.web;

import wicket.spring.SpringWebApplication;
import wicket.spring.common.ContactDao;

/**
 * Application class for our examples
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class ExampleApplication extends SpringWebApplication {

	/**
	 * this field holds a contact dao proxy that is safe to use in wicket
	 * components
	 */
	private ContactDao contactDaoProxy;

	/**
	 * this field holds the actual contact dao retrieved from spring context.
	 * this object should never be serialized because it will take the container
	 * with it, so BE CAREFUL when using this.
	 */
	private ContactDao contactDao;

	/**
	 * Retrieves contact dao bean. This bean should not be serialized so BE
	 * CAREFUL when using it.
	 * 
	 * @return contact dao bean
	 */
	public ContactDao getContactDao() {
		if (contactDao == null) {
			synchronized (this) {
				if (contactDao == null) {
					contactDao = (ContactDao) internalGetApplicationContext()
							.getBean("contactDao", ContactDao.class);
				}
			}
		}
		return contactDao;
	}

	/**
	 * Returns a lazy init proxy for the dao bean. This proxy is safe to
	 * serialize and will take up very little space when serialized.
	 * 
	 * @return a lazy init proxy for the dao bean
	 */
	public ContactDao getContactDaoProxy() {
		if (contactDaoProxy == null) {
			synchronized (this) {
				if (contactDaoProxy == null) {
					contactDaoProxy = (ContactDao) createSpringBeanProxy(
							ContactDao.class, "contactDao");
				}
			}
		}
		return contactDaoProxy;
	}

	public Class getHomePage() {
		return HomePage.class;
	}

}
