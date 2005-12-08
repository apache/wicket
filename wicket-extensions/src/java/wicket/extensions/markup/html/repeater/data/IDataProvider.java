/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.repeater.data;

import java.io.Serializable;
import java.util.Iterator;

import wicket.model.IModel;

/**
 * Interface used to provide data to data views
 * <p>
 * Example:
 * 
 * <pre>
 *   class UsersProvider implements IDataProvider() {
 *     
 *     Iterator iterator(int first, int count) {
 *       ((MyApplication)Application.get()).getUserDao().iterator(first, count);
 *     }
 *     
 *     int size() {
 *       ((MyApplication)Application.get()).getUserDao().getCount();
 *     }
 *     
 *     IModel model(Object object) {
 *       return new DetachableUserModel((User)object);
 *     }
 *   }
 * </pre>
 * 
 * @see DataViewBase
 * @see DataView
 * @see GridView
 * 
 * @author Igor Vaynberg
 * 
 */
public interface IDataProvider extends Serializable
{
	/**
	 * Gets an iterator for the subset of total data
	 * 
	 * @param first
	 *            first row of data
	 * @param count
	 *            minumum number of elements to retrieve
	 * 
	 * @return iterator capable of iterating over {first, first+count} items
	 */
	Iterator iterator(int first, int count);

	/**
	 * Gets total number of items in the collection represented by the
	 * DataProvider
	 * 
	 * @return total item count
	 */
	int size();

	/**
	 * Converts the object in the collection to its model representation. This
	 * is a good place to wrap your objects in a detachable model.
	 * 
	 * @param object
	 *            the object that needs to be wrapped
	 * 
	 * @return the model representation of the object
	 */
	IModel model(Object object);

}
