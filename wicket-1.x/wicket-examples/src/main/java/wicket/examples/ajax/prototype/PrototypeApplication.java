/*
 * $Id$ $Revision:
 * 4323 $ $Date$
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
package wicket.examples.ajax.prototype;

import wicket.examples.WicketExampleApplication;
import wicket.markup.html.AjaxServerAndClientTimeFilter;
import wicket.markup.html.ServerAndClientTimeFilter;

/**
 * Application object for the Prototype.js ajax demo page.
 * 
 * @author ivaynberg
 */
public class PrototypeApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public PrototypeApplication()
	{
	}

	/**
	 * @see wicket.examples.WicketExampleApplication#init()
	 */
	protected void init()
	{
		getExceptionSettings().setThrowExceptionOnMissingResource(false);
		getRequestCycleSettings().addResponseFilter(new AjaxServerAndClientTimeFilter());
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Index.class;
	}
}