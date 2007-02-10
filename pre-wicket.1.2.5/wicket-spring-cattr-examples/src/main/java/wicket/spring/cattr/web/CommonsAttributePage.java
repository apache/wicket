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

import wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import wicket.spring.common.ContactDao;
import wicket.spring.common.web.ContactsDisplayPage;

/**
 * Test page that displays the use of commons attributes for dependency
 * injection. Note the javadoc tag at the dao field; this is the commons
 * attribute that denotes it is a spring dependency.
 *
 * @author ivaynberg
 */
public class CommonsAttributePage extends ContactsDisplayPage {

	/**
	 * @@wicket.spring.injection.cattr.SpringBean("contactDao")
	 */
	private ContactDao dao;

	/**
	 * Construct.
	 */
	public CommonsAttributePage() {
	}

	/**
	 * @see wicket.spring.common.web.ContactsDisplayPage#getDataProvider()
	 */
	protected SortableDataProvider getDataProvider() {
		return new ProxyDataProvider(dao);
	}
}
