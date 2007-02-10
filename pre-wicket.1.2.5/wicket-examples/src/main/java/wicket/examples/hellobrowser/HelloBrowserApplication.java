/*
 * $Id: HelloBrowserApplication.java 3646 2006-01-04 21:32:14 +0000 (Wed, 04 Jan
 * 2006) ivaynberg $ $Revision$ $Date: 2006-01-04 21:32:14 +0000 (Wed, 04
 * Jan 2006) $
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
package wicket.examples.hellobrowser;

import wicket.examples.WicketExampleApplication;

/**
 * Application class for hello browser example.
 * 
 * @author Eelco Hillenius
 */
public class HelloBrowserApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public HelloBrowserApplication()
	{
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return HelloBrowser.class;
	}

	/**
	 * @see wicket.examples.WicketExampleApplication#init()
	 */
	protected void init()
	{
		getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
	}
}
