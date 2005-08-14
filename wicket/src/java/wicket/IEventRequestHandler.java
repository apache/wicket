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
package wicket;

import wicket.markup.ComponentTag;

/**
 * Handles event requests, like AJAX (XmlHttp) requests.
 *
 * @author Eelco Hillenius
 */
public interface IEventRequestHandler
{
	/**
	 * gets the id of this handler. defaults to the objects' hashcode.
	 * @return the id of this handler
	 */
	String getId();

	/**
	 * This method is called everytime a this handler is added to a component.
	 * By default this does nothing.
	 * @param component the component this handler is binded to
	 */
	void bind(Component component);

	/**
	 * Called any time a component that has this handler registered is rendering the component tag.
	 * Use this method e.g. to bind to javascript event handlers of the tag
	 * @param component the component
	 * @param tag the tag that is rendered
	 */
	void onComponentTag(Component component, ComponentTag tag);

	/**
	 * Called to indicate that the component that has this handler registered has been rendered.
	 * Use this method to do any cleaning up of temporary state
	 * @param component the component
	 */
	void rendered(Component component);

	/**
	 * Called when a event request (e.g. XmlHttpRequest) was received.
	 */
	void onEventRequest();
}