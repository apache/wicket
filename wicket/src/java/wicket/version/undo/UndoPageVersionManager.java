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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.Page;
import wicket.util.collections.ArrayListStack;
import wicket.version.IPageVersionManager;

/**
 * A version manager implemented by recording component changes as undo records
 * which can later be reversed to get back to a given version of the page being
 * managed.
 * 
 * @author Jonathan Locke
 */
public class UndoPageVersionManager implements IPageVersionManager
{
	/** log. */
	private static final Log log = LogFactory.getLog(UndoPageVersionManager.class);

	private static final long serialVersionUID = 1L;

	/** The current list of changes */
	private ChangeList changeList;

	/** Stack of change lists for undoing */
	private final ArrayListStack changeListStack = new ArrayListStack();

	/** The current version number */
	private int currentVersionNumber = 0;
	
	/** Maximum number of most-recent versions to keep */
	private final int maxVersions;

	/** The page being managed */
	private final Page page;

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
		this.page = page;
		this.maxVersions = maxVersions;
	}

	/**
	 * @see wicket.version.IPageVersionManager#beginVersion()
	 */
	public void beginVersion()
	{
		// Create new change list
		changeList = new ChangeList();

		// We are working on the next version now
		currentVersionNumber++;
	}

	/**
	 * @see wicket.version.IPageVersionManager#componentAdded(wicket.Component)
	 */
	public void componentAdded(Component component)
	{
		changeList.componentAdded(component);
	}

	/**
	 * @see wicket.version.IPageVersionManager#componentModelChanging(wicket.Component)
	 */
	public void componentModelChanging(Component component)
	{
		changeList.componentModelChanging(component);
	}

	/**
	 * @see wicket.version.IPageVersionManager#componentRemoved(wicket.Component)
	 */
	public void componentRemoved(Component component)
	{
		changeList.componentRemoved(component);
	}

	/**
	 * @see wicket.version.IPageVersionManager#componentStateChanging(wicket.version.undo.Change)
	 */
	public void componentStateChanging(Change change)
	{
		changeList.componentStateChanging(change);
	}

	/**
	 * @see wicket.version.IPageVersionManager#endVersion()
	 */
	public void endVersion()
	{
		// Push change list onto stack
		changeListStack.push(changeList);
		
		// If stack is overfull, remove oldest entry
		if (getVersions() > maxVersions)
		{
			expireOldestVersion();
		}

		// Make memory efficient for replication
		changeListStack.trimToSize();

		if (log.isDebugEnabled())
		{
			log.debug("Version " + currentVersionNumber + " for page " + page + " stored");
		}
	}
	
	/**
	 * Expires an old version
	 */
	public void expireOldestVersion()
	{
		changeListStack.remove(0);
	}

	/**
	 * @see wicket.version.IPageVersionManager#getCurrentVersionNumber()
	 */
	public int getCurrentVersionNumber()
	{
		return currentVersionNumber;
	}

	/**
	 * @see wicket.version.IPageVersionManager#getVersion(int)
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
	 * @see wicket.version.IPageVersionManager#getVersions()
	 */
	public int getVersions()
	{
		return changeListStack.size();
	}

	/**
	 * Goes back a version from the current version
	 * 
	 * @return True if the page was successfully reverted to its previous
	 *         version
	 */
	private boolean undo()
	{
		if (log.isDebugEnabled())
		{
			log.debug("UNDO: rollback " + page + " to version " + currentVersionNumber);
		}

		if(changeListStack.isEmpty())
		{
		    return false;
		}

		// Pop off top change list
		final ChangeList changeList = (ChangeList)changeListStack.pop();
		if (changeList == null)
		{
			return false;
		}

		// Undo changes made to previous version to get to this version
		changeList.undo();

		// One less version around
		currentVersionNumber--;
		return true;
	}
}
