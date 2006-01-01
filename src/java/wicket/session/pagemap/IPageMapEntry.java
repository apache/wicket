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
package wicket.session.pagemap;

import java.io.Serializable;

import wicket.Page;

/**
 * Some source which produces a page. Page implements IPageMapEntry by simply
 * returning "this", but other implementations are possible as well, allowing
 * users to create IPageMapEntry implementations that reconstruct full blown
 * Page objects from a limited set of data (for example, a details page from an
 * id). The advantage of doing this is that you can save session memory (by
 * trading off against the processing power required to reconstruct the page).
 * 
 * @see wicket.AbstractPageMapEntry
 * @author Jonathan Locke
 */
public interface IPageMapEntry extends Serializable
{
	/**
	 * @return A sequence number that can be used to compare when two page map
	 *         entries were accessed. Given two entries, the one with the higher
	 *         access sequence number was the one more recently accessed.
	 */
	public int getAccessSequenceNumber();

	/**
	 * @return A stable identifier for this page map entry
	 */
	public int getNumericId();

	/**
	 * @return Gets the page, possibly creating it on the fly.
	 */
	public Page getPage();

	/**
	 * @return The class of page stored in this page map entry (which can be
	 *         used by an eviction strategy to prioritize evictions)
	 */
	public Class getPageClass();

	/**
	 * @return True if this entry is dirty and requires replication
	 */
	public boolean isDirty();

	/**
	 * @param accessSequenceNumber
	 *            New access sequence numnber for this entry
	 */
	public void setAccessSequenceNumber(int accessSequenceNumber);

	/**
	 * @param dirty
	 *            True if this entry is now dirty
	 */
	public void setDirty(boolean dirty);

	/**
	 * @param id
	 *            The numeric id for this entry
	 */
	public void setNumericId(int id);
}
