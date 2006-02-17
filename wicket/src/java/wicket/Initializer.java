/*
 * $Id$
 * $Revision$ $Date$
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
package wicket;

import wicket.ajax.AjaxInitializer;
import wicket.behavior.IBehaviorListener;
import wicket.markup.html.form.IFormSubmitListener;
import wicket.markup.html.form.IOnChangeListener;
import wicket.markup.html.link.ILinkListener;
import wicket.markup.html.tree.TreeComponentInitializer;

/**
 * Initializer for components in wicket core library.
 * 
 * @author Jonathan Locke
 */
public class Initializer implements IInitializer
{
	/**
	 * @see wicket.IInitializer#init(wicket.Application)
	 */
	public void init(Application application)
	{
		
		// touch all interfaces once, so that they are registered for the complete webapplication
		Object o = IBehaviorListener.INTERFACE;
		o = IFormSubmitListener.INTERFACE;
		o = IOnChangeListener.INTERFACE;
		o = ILinkListener.INTERFACE;
		o = IRedirectListener.INTERFACE;
		o = IResourceListener.INTERFACE;

		// initialize the tree component
		new TreeComponentInitializer().init(application);
		
		// initialize ajax components
		new AjaxInitializer().init(application);
	}
}
