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
package wicket.markup.html.form;

import java.io.Serializable;

/**
 * Renders one choice. Seperates the 'id' values used for internal
 * representation from 'display values' which are the values shown to the user
 * of components that use this renderer.
 * 
 * @param <T>
 *            The type
 * 
 * @author jcompagner
 */
public interface IChoiceRenderer<T> extends Serializable
{
	/**
	 * Get the value for displaying to an end user.
	 * 
	 * @param object
	 *            the actual object
	 * @return the value meant for displaying to an end user
	 */
	Object getDisplayValue(T object);

	/**
	 * This method is called to get the id value of an object (used as the value
	 * attribute of a choice element) The id can be extracted from the object
	 * like a primary key, or if the list is stable you could just return a
	 * toString of the index.
	 * 
	 * @param object
	 *            The object for which the id should be generated
	 * @param index
	 *            The index of the object in the choices list.
	 * @return String
	 */
	String getIdValue(T object, int index);
}