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
package wicket.markup.html.form;

/**
 * Renders one choice. Seperates the 'id' values used for internal representation from
 * 'display values' which are the values show to the user of components that use this renderer.
 *
 * @author jcompagner
 */
public interface IChoiceRenderer
{
	/**
	 * Get the value for displaying to an end user.
	 *
	 * @param object the actual object
	 * @return the value meant for displaying to an end user
	 */
	public String getDisplayValue(Object object);
	
	/**
	 * Gets the value meant for interal representation.
	 *
	 * @param object the actual object
	 * @param index
	 * @return the value meant for interal representation
	 */
	public String getIdValue(Object object, int index);
}
