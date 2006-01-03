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
package wicket.model;

/**
 * Implementations of this interface compare model object.
 * 
 * @author jcompagner
 * @author Jonathan Locke
 */
public interface IModelComparator
{
	/**
	 * @param a
	 *            Model object a
	 * @param b
	 *            Model object b
	 * @return True if the two model objects are equal
	 */
	boolean compare(Object a, Object b);
}
