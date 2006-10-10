/*
 * $Id: IListenerInterfaceRequestTarget.java,v 1.2 2005/11/30 21:43:01 joco01
 * Exp $ $Revision$ $Date$
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
package wicket.request.target.component.listener;

import wicket.Component;
import wicket.RequestListenerInterface;
import wicket.request.RequestParameters;
import wicket.request.target.component.IPageRequestTarget;

/**
 * Target that denotes a page instance and a call to a component on that page
 * using an listener interface method.
 * 
 * @author Eelco Hillenius
 */
public interface IListenerInterfaceRequestTarget extends IPageRequestTarget
{
	/**
	 * Gets the target component.
	 * 
	 * @return the target component
	 */
	Component getTarget();

	/**
	 * Gets listener method.
	 * 
	 * @return the listener method
	 */
	RequestListenerInterface getRequestListenerInterface();

	/**
	 * Get the request parameters
	 * 
	 * @return The request parameters 
	 */
	RequestParameters getRequestParameters();
}
