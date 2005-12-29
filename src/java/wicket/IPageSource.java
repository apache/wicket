/*
 * $Id$ $Revision:
 * 1.3 $ $Date$
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

import java.io.Serializable;

/**
 * Some source which produces a page. Page implements IPageSource by simply
 * returning "this", but other implementations are possible as well, allowing
 * users to create IPageSource implementations that reconstruct full blown Page
 * objects from a limited set of data (for example, a details page from an id).
 * The advantage of doing this is that you can save session memory (by trading
 * off against the processing power required to reconstruct the page).
 * 
 * @author Jonathan Locke
 */
public interface IPageSource extends Serializable
{
	/**
	 * @return The Page.
	 */
	public Page getPage();

	/**
	 * @return The id of the page returned by this page source
	 */
	public int getNumericId();

	/**
	 * @return A sequence number indicating when this page source was most
	 *         recently accessed.
	 */
	public int getAccessSequenceNumber();

	/**
	 * @param accessSequenceNumber
	 *            New access sequence numnber for this page source
	 */
	public void setAccessSequenceNumber(int accessSequenceNumber);

	/**
	 * @return True if this page source is dirty and requires replication
	 */
	public boolean isDirty();

	/**
	 * @param dirty
	 *            True if this page source is now dirty
	 */
	public void setDirty(boolean dirty);
}
