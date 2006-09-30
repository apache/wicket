/*
 * $Id: ExamplePage.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 13:36:52 +0000 (Sun, 16 Apr
 * 2006) $
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

import wicket.examples.WicketExamplePage;

/**
 * Base class for all pages in the QuickStart application. Any page which
 * subclasses this page can get session properties from QuickStartSession via
 * getQuickStartSession().
 * 
 * @param <T> 
 */
public abstract class ExamplePage<T> extends WicketExamplePage<T> 
{
	/**
	 * Construct 
	 */
	public ExamplePage()
	{
		newWicketExampleHeader(this);
	}
	
	/**
	 * @see wicket.examples.WicketExamplePage#addWicketExampleHeader()
	 */
	@Override
	protected void addWicketExampleHeader()
	{
		// We manually add the example header
		return;
	}
	
	/**
	 * Get downcast session object for easy access by subclasses
	 * 
	 * @return The session
	 */
	public ContactsDatabase getContactsDB()
	{
		return ((RepeaterApplication)getApplication()).getContactsDB();
	}
}