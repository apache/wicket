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
 * 
 * @author Jonathan Locke
 */
public class UndoPageVersionManager implements IPageVersionManager
{
	/** Current version of page */
	private int version = 0;
	
	/**
	 * Constructor
	 * 
	 * @param page
	 *            The page that we're versioning
	 */
	public UndoPageVersionManager(final Page page)
	{
	}

	/**
	 * @see wicket.IPageVersionManager#beginVersion()
	 */
	public void beginVersion()
	{
		version++;
	}

	/**
	 * @see wicket.IPageVersionManager#componentAdded(wicket.Component)
	 */
	public void componentAdded(Component component)
	{
	}

	/**
	 * @see wicket.IPageVersionManager#componentModelChanged(wicket.Component)
	 */
	public void componentModelChanged(Component component)
	{
	}

	/**
	 * @see wicket.IPageVersionManager#componentRemoved(wicket.Component)
	 */
	public void componentRemoved(Component component)
	{
	}

	/**
	 * @see wicket.IPageVersionManager#endVersion()
	 */
	public void endVersion()
	{
	}

	/**
	 * @see wicket.IPageVersionManager#getVersion(int)
	 */
	public Page getVersion(int version)
	{
		return null;
	}

	/**
	 * @see wicket.IPageVersionManager#getVersion()
	 */
	public int getVersion()
	{
		return version;
	}
}
