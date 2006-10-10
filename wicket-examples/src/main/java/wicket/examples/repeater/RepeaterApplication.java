/*
 * $Id$ $Revision:
 * 4917 $ $Date$
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

import wicket.markup.html.ServerAndClientTimeFilter;
import wicket.protocol.http.WebApplication;

/**
 * application class for repeater examples application
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class RepeaterApplication extends WebApplication
{
	private ContactsDatabase contactsDB = new ContactsDatabase(50);


	/**
	 * Constructor.
	 */
	public RepeaterApplication()
	{
	}

	/**
	 * @see wicket.protocol.http.WebApplication#init()
	 */
	protected void init()
	{
		getRequestCycleSettings().addResponseFilter(new ServerAndClientTimeFilter());
	}

	/**
	 * @return contacts database
	 */
	public ContactsDatabase getContactsDB()
	{
		return contactsDB;
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Index.class;
	}


}
