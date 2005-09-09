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
package wicket.version.undo;

import wicket.Component;

/**
 * An add change operation.
 * 
 * @author Jonathan Locke
 */
class Add extends Change
{
	private final Component component;
	
	Add(final Component component)
	{
		this.component = component;
	}
	
	/**
	 * @see wicket.version.undo.Change#undo()
	 */
	public void undo()
	{
		component.remove();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "Add[component: " + component.getId() + ", parent: " + component.getParent().getId() + "]";
	}
}
