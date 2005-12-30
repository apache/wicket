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
 * An abstract base class that makes it easier to create IPageSource
 * implementations.
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractPageSource implements IPageSource
{
	private short accessSequenceNumber;
	private boolean dirty;
	private short id;
	
	/**
	 * @see wicket.IPageSource#getAccessSequenceNumber()
	 */
	public int getAccessSequenceNumber()
	{
		return accessSequenceNumber;
	}

	/**
	 * @see wicket.IPageSource#getNumericId()
	 */
	public int getNumericId()
	{
		return id;
	}

	/**
	 * @return The Page.
	 */
	public abstract Page getPage();

	/**
	 * @see wicket.IPageSource#isDirty()
	 */
	public boolean isDirty()
	{
		return dirty;
	}

	/**
	 * @see wicket.IPageSource#setAccessSequenceNumber(int)
	 */
	public void setAccessSequenceNumber(int accessSequenceNumber)
	{
		this.accessSequenceNumber = (short)this.accessSequenceNumber;
	}

	/**
	 * @see wicket.IPageSource#setDirty(boolean)
	 */
	public void setDirty(boolean dirty)
	{
		this.dirty = dirty;
	}
	
	/**
	 * @see wicket.IPageSource#setNumericId(int)
	 */
	public void setNumericId(int id)
	{
		this.id = (short)id;
	}
}
