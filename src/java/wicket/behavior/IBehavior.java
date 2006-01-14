/*
 * $Id$ $Revision$ $Date$
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
package wicket.behavior;

import java.io.Serializable;

import wicket.Component;
import wicket.markup.ComponentTag;

/**
 * Behaviors are kind of plug-ins for Components. They allow to be added to a
 * component and get essential events forwarded by the component. Currently they
 * can be bound to a concrete component (the bind method is called when the
 * behavior is attached), but they don't need to. They can modify the
 * components markup by changing the rendered ComponentTag. Behaviors could
 * have own models as well, they are notified when these are to be detached by
 * the component.
 * 
 * @author Ralf Ebert
 * @author Eelco Hillenius
 */
public interface IBehavior extends Serializable
{
	/**
	 * Bind this handler to the given component.
	 * 
	 * @param hostComponent
	 *            the component to bind to
	 */
	void bind(Component hostComponent);

	/**
	 * Detaches all models, called by components which have this behavior
	 * attached
	 */
	void detachModel();

	/**
	 * Called any time a component that has this behavior registered is
	 * rendering the component tag.
	 * 
	 * @param component
	 *            the component that renders this tag currently
	 * @param tag
	 *            the tag that is rendered
	 */
	void onComponentTag(Component component, ComponentTag tag);

	/**
	 * Called when a component that has this behavior coupled was rendered.
	 * 
	 * @param hostComponent
	 *            the component that has this behavior coupled
	 */
	void rendered(Component hostComponent);
}