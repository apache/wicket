/*
 * $Id$ $Revision:
 * 3646 $ $Date$
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
package wicket.examples.nested;

import wicket.examples.WicketExampleApplication;

/**
 * WicketServlet class for nested structure example.
 * 
 * @author Eelco Hillenius
 */
public class NestedApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public NestedApplication()
	{
	}
	
	/**
	 * Initialize the application
	 */
	protected void init()
	{
		super.init();
		// disable debugging mode, because it slows down the tree
		getAjaxSettings().setAjaxDebugModeEnabled(false);
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Home.class;
	}

}
