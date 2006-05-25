/*
 * $Id: ISortState.java 3399 2005-12-09 07:43:11 +0000 (Fri, 09 Dec 2005)
 * ivaynberg $ $Revision$ $Date: 2005-12-09 07:43:11 +0000 (Fri, 09 Dec
 * 2005) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.repeater.data.sort;

import java.io.Serializable;

/**
 * Interface used by OrderByLink to interact with any object that keeps track of
 * sorting state
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface ISortState extends Serializable
{
	/**
	 * property state representing ascending sort order
	 */
	public static final int ASCENDING = 1;
	/**
	 * property state representing descending sort order
	 */
	public static final int DESCENDING = -1;
	/**
	 * property state presenting not-sorted sort order
	 */
	public static final int NONE = 0;

	/**
	 * Sets sort order of the property
	 * 
	 * @param property
	 *            the name of the property to sort on
	 * @param state
	 *            new sort state of the property. must be one of ASCENDING,
	 *            DESCENDING, or NONE
	 */
	public void setPropertySortOrder(String property, int state);

	/**
	 * Gets the sort order of a property
	 * 
	 * @param property
	 *            sort property to be checked
	 * @return one of ASCENDING, DESCENDING, or NONE
	 */
	public int getPropertySortOrder(String property);

}
