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
package wicket.markup.html.form.model;

/**
 * Interface implemented by choices in an IChoiceList.
 * 
 * @author Jonathan Locke
 */
public interface IChoice
{
	/**
	 * Gets the display value for the choice
	 * 
	 * @return The display value
	 */
	public String getDisplayValue();

	/**
	 * @return The id for this choice
	 */
	public String getId();
	
	/**
	 * @return The actual object
	 */
	public Object getObject();
}
