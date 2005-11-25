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
package wicket.request;

import java.lang.reflect.Method;

import wicket.Component;

/**
 * Target that denotes a page instance and a call to a component on that page
 * using an listener interface method.
 * 
 * @author Eelco Hillenius
 */
public interface IInterfaceCallRequestTarget extends IPageRequestTarget, ISessionSynchronizable
{

	/**
	 * Gets the target component.
	 * 
	 * @return the target component
	 */
	public abstract Component getComponent();

	/**
	 * Gets listener method.
	 * 
	 * @return the listener method
	 */
	public abstract Method getListenerMethod();

}