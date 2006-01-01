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
package wicket;

/**
 * An abstract base class that makes it easier to create IPageMapEntry
 * implementations.
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractPageMapEntry implements IPageMapEntry
{
	private short accessSequenceNumber;
	private boolean dirty;
	private short id;

	/**
	 * @see wicket.IPageMapEntry#getAccessSequenceNumber()
	 */
	public int getAccessSequenceNumber()
	{
		return accessSequenceNumber;
	}

	/**
	 * @see wicket.IPageMapEntry#getNumericId()
	 */
	public int getNumericId()
	{
		return id;
	}

	/**
	 * @see wicket.IPageMapEntry#getPage()
	 */
	public abstract Page getPage();

	/**
	 * Failing to override this method could be pretty expensive because this
	 * default implementation calls getPage(), which probably creates the page.
	 * That's a lot of work to do in order to simply determine the page's class.
	 * So, if there's an easy way to implement this method, that might be
	 * desirable.
	 * 
	 * @see wicket.IPageMapEntry#getPageClass()
	 */
	public Class getPageClass()
	{
		return getPage().getClass();
	}

	/**
	 * @see wicket.IPageMapEntry#isDirty()
	 */
	public boolean isDirty()
	{
		return dirty;
	}

	/**
	 * @see wicket.IPageMapEntry#setAccessSequenceNumber(int)
	 */
	public void setAccessSequenceNumber(int accessSequenceNumber)
	{
		this.accessSequenceNumber = (short)this.accessSequenceNumber;
	}

	/**
	 * @see wicket.IPageMapEntry#setDirty(boolean)
	 */
	public void setDirty(boolean dirty)
	{
		this.dirty = dirty;
	}

	/**
	 * @see wicket.IPageMapEntry#setNumericId(int)
	 */
	public void setNumericId(int id)
	{
		this.id = (short)id;
	}
}
