/*
 * $Id: IItemReuseStrategy.java 5679 2006-05-07 18:16:52 +0000 (Sun, 07 May
 * 2006) ivaynberg $ $Revision$ $Date: 2006-05-07 18:16:52 +0000 (Sun, 07
 * May 2006) $
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
package wicket.extensions.markup.html.repeater.refreshing;

import java.io.Serializable;
import java.util.Iterator;

import wicket.MarkupContainer;

/**
 * Interface for item reuse strategies.
 * <p>
 * <u>Notice:</u> Child items will be rendered in the order they are provided
 * by the returned iterator, so it is important that the strategy preserve this
 * order
 * </p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IItemReuseStrategy extends Serializable
{

	/**
	 * Returns an iterator over items that will be added to the view. The
	 * iterator needs to return all the items because the old ones are removed
	 * prior to the new ones added.
	 * @param container 
	 * 
	 * @param factory
	 *            implementation of IItemFactory
	 * @param newModels
	 *            iterator over models for items
	 * @param existingItems
	 *            iterator over child items
	 * @return iterator over items that will be added after all the old items
	 *         are moved.
	 */
	Iterator getItems(MarkupContainer container, IItemFactory factory, Iterator newModels, Iterator existingItems);
}
