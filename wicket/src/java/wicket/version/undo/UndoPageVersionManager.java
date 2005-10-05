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
package wicket.version.undo;

import java.util.LinkedHashMap;
import java.util.Map;

import wicket.Component;
import wicket.IPageVersionManager;
import wicket.Page;

/**
 * A version manager implemented by recording component changes as undo records
 * which can later be reversed to get back to a given version of the page being
 * managed.
 * 
 * @author Jonathan Locke
 */
public class UndoPageVersionManager implements IPageVersionManager
{
	private static final long serialVersionUID = 1L;
	
	/** The current list of changes */
	private ChangeList changeList;

	/**
	 * Holds the change list that was applied to a given version number. For
	 * example, the ChangeList that was /applied/ to version 0 would be stored
	 * under the key Integer(0).
	 */
	private final Map appliedChangeListForVersionNumber;

	/** The page being managed */
	private final Page page;

	/** The current version number */
	private int versionNumber = 0;

	/**
	 * Constructor
	 * 
	 * @param page
	 *            The page that we're tracking changes to
	 * @param maxVersions
	 *            The maximum number of versions to maintain before expiring the
	 *            old versions
	 */
	public UndoPageVersionManager(final Page page, final int maxVersions)
	{
		// Save page that this version manager is working on
		this.page = page;

		// Create an insertion-ordered MRU map
		this.appliedChangeListForVersionNumber = new LinkedHashMap()
		{
			private static final long serialVersionUID = 1L;
			
			protected boolean removeEldestEntry(final Map.Entry ignored)
			{
				// Tell collections class to remove oldest entry if there are
				// more than maxVersions entries
				return size() > maxVersions;
			}
		};
	}

	/**
	 * @see wicket.IPageVersionManager#beginVersion()
	 */
	public void beginVersion()
	{
		// Create new change list
		changeList = new ChangeList();

		// We are working on the next version now
		versionNumber++;
	}

	/**
	 * @see wicket.IPageVersionManager#componentAdded(wicket.Component)
	 */
	public void componentAdded(Component component)
	{
		changeList.componentAdded(component);
	}

	/**
	 * @see wicket.IPageVersionManager#componentModelChanging(wicket.Component)
	 */
	public void componentModelChanging(Component component)
	{
		changeList.componentModelChanging(component);
	}

	/**
	 * @see wicket.IPageVersionManager#componentStateChanging(wicket.version.undo.Change)
	 */
	public void componentStateChanging(Change change)
	{
		changeList.componentStateChanging(change);
	}
	
	/**
	 * @see wicket.IPageVersionManager#componentRemoved(wicket.Component)
	 */
	public void componentRemoved(Component component)
	{
		changeList.componentRemoved(component);
	}

	/**
	 * @see wicket.IPageVersionManager#endVersion()
	 */
	public void endVersion()
	{
		// Store change list under key for previous version, since the change
		// list is the set of changes to /get/ to the current version.
		appliedChangeListForVersionNumber.put(new Integer(getCurrentVersionNumber() - 1),
				changeList);
	}

	/**
	 * @see wicket.IPageVersionManager#getCurrentVersionNumber()
	 */
	public int getCurrentVersionNumber()
	{
		return versionNumber;
	}

	/**
	 * @see wicket.IPageVersionManager#getVersion(int)
	 */
	public Page getVersion(final int versionNumber)
	{
		// If the requested version is at or before the current version
		if (versionNumber <= getCurrentVersionNumber())
		{
			// Loop until we reach the right version
			while (getCurrentVersionNumber() > versionNumber)
			{
				// Go back one version
				if (!undo())
				{
					return null;
				}
			}

			// Return modified page
			return page;
		}
		else
		{
			// The version is not available
			return null;
		}
	}

	/**
	 * Goes back a version from the current version
	 * 
	 * @return True if the page was successfully reverted to its previous
	 *         version
	 */
	private boolean undo()
	{
		// Get the change list that was applied to the previous version
		final Integer key = new Integer(getCurrentVersionNumber() - 1);
		final ChangeList changeList = (ChangeList)appliedChangeListForVersionNumber.get(key);
		if (changeList == null)
		{
			return false;
		}

		// Undo changes made to previous version to get to this version
		changeList.undo();

		// Remove version from change list map
		appliedChangeListForVersionNumber.remove(key);

		// One less version around
		versionNumber--;
		return true;
	}
}
