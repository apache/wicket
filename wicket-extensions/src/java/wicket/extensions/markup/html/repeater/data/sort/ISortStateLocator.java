/*
 * $Id: ISortStateLocator.java 5396 2006-04-16 16:56:26 +0000 (Sun, 16 Apr 2006)
 * ivaynberg $ $Revision$ $Date: 2006-04-16 16:56:26 +0000 (Sun, 16 Apr
 * 2006) $
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
 * Locator interface for ISortState implementations. OrderByLink uses this
 * interface to locate and version ISortState objects.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface ISortStateLocator extends Serializable
{
	/**
	 * @return ISortState object
	 */
	ISortState getSortState();

	/**
	 * Setter for the sort state object
	 * 
	 * @param state
	 *            new sort state
	 */
	void setSortState(ISortState state);
}
