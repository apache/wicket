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


/**
 * Implementations of this interface can be used in the Component.getComparator()
 * for testing the current value of the components model data with the new value
 * that is given.
 * 
 * @author jcompagner
 * 
 */
public interface IComponentValueComparator
{
	/**
	 * @param component
	 *            The component for which the compare must take place.
	 * @param newObject
	 *            The object to compare the current value to.
	 * @return true if the current component model value is the same as the
	 *         newObject.
	 */
	boolean compareValue(Component component, Object newObject);
}
