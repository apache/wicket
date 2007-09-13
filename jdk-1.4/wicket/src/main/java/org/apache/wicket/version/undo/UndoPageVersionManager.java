/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.version.undo;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.util.collections.ArrayListStack;
import org.apache.wicket.version.IPageVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A version manager implemented by recording <code>Component</code> changes
 * as undo records. These records can later be reversed to get back to a given
 * version of the <code>Page</code> being managed.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public class UndoPageVersionManager implements IPageVersionManager
{
	/** logger */
	private static final Logger log = LoggerFactory.getLogger(UndoPageVersionManager.class);

	private static final long serialVersionUID = 1L;

	/** the current list of changes */
	private ChangeList changeList;

	/** the stack of change lists for undoing */
	private final ArrayListStack changeListStack = new ArrayListStack();

	/** the current version number */
	private int currentVersionNumber = 0;

	/** the current Ajax version number */
	private int currentAjaxVersionNumber = 0;

	/** maximum number of most-recent versions to keep */
	private final int maxVersions;

	/** the <code>Page</code> being managed */
	private final Page page;

	/**
	 * If this is true, the version that was created is not merged with the
	 * previous one.
	 */
	private transient boolean ignoreMerge = false;

	/**
	 * Constructor.
	 * 
	 * @param page
	 *            the <code>Page</code> that we're tracking changes to
	 * @param maxVersions
	 *            the maximum number of versions to maintain before expiring
	 *            old versions
	 */
	public UndoPageVersionManager(final Page page, final int maxVersions)
	{
		this.page = page;
		this.maxVersions = maxVersions;
	}

	/**
	 * @see IPageVersionManager#beginVersion(boolean)
	 */
	public void beginVersion(boolean mergeVersion)
	{
		// Create new change list.
		changeList = new ChangeList();

		// If we merge, then the version number shouldn't be upgraded.
		if(!mergeVersion)
		{
			// We are working on the next version now.
			currentVersionNumber++;
			currentAjaxVersionNumber = 0;
		}
		else
		{
			currentAjaxVersionNumber++;
		}
	}
	
	/**
	 * @see IPageVersionManager#ignoreVersionMerge()
	 */
	public void ignoreVersionMerge()
	{
		ignoreMerge = true;
		currentVersionNumber++;
		currentAjaxVersionNumber = 0;
	}

	/**
	 * @see IPageVersionManager#componentAdded(Component)
	 */
	public void componentAdded(Component component)
	{
		changeList.componentAdded(component);
	}

	/**
	 * @see IPageVersionManager#componentModelChanging(Component)
	 */
	public void componentModelChanging(Component component)
	{
		changeList.componentModelChanging(component);
	}

	/**
	 * @see IPageVersionManager#componentRemoved(Component)
	 */
	public void componentRemoved(Component component)
	{
		changeList.componentRemoved(component);
	}

	/**
	 * @see IPageVersionManager#componentStateChanging(Change)
	 */
	public void componentStateChanging(Change change)
	{
		changeList.componentStateChanging(change);
	}

	/**
	 * @see IPageVersionManager#endVersion(boolean)
	 */
	public void endVersion(boolean mergeVersion)
	{
		if(mergeVersion && !ignoreMerge)
		{
			if(changeListStack.size() > 0)
			{
				ChangeList previous = (ChangeList)changeListStack.peek();
				previous.add(changeList);
			}
		}
		else
		{
			ignoreMerge = false;
			
			// Push change list onto stack.
			changeListStack.push(changeList);
			
			// If stack is overfull, remove oldest entry.
			if (getVersions() > maxVersions)
			{
				expireOldestVersion();
			}
	
			// Make memory efficient for replication.
			changeListStack.trimToSize();
	
			if (log.isDebugEnabled())
			{
				log.debug("Version " + currentVersionNumber + " for page " + page + " stored");
			}
		}
	}
	
	/**
	 * @see IPageVersionManager#expireOldestVersion()
	 */
	public void expireOldestVersion()
	{
		changeListStack.remove(0);
	}

	/**
	 * @see IPageVersionManager#getCurrentVersionNumber()
	 */
	public int getCurrentVersionNumber()
	{
		return currentVersionNumber;
	}
	
	/**
	 * @see IPageVersionManager#getAjaxVersionNumber()
	 */
	public int getAjaxVersionNumber()
	{
		return currentAjaxVersionNumber;
	}

	/**
	 * @see IPageVersionManager#getVersion(int)
	 */
	public Page getVersion(final int versionNumber)
	{
		// If the requested version is at or before the current version,
		if (versionNumber <= getCurrentVersionNumber())
		{
			// loop until we reach the right version.
			while (getCurrentVersionNumber() > versionNumber)
			{
				// Go back one version.
				if (!undo())
				{
					return null;
				}
			}

			// Return modified page.
			return page;
		}
		else
		{
			// The version is not available.
			return null;
		}
	}
	
	/**
	 * @see IPageVersionManager#rollbackPage(int)
	 */
	public Page rollbackPage(int numberOfVersions)
	{
		// TODO NEEDS IMPL! See SecondLevelCache PageMap impl
		return null;
	}

	/**
	 * @see IPageVersionManager#getVersions()
	 */
	public int getVersions()
	{
		return changeListStack.size();
	}

	/**
	 * Goes back a <code>Page</code> version from the current version.
	 * 
	 * @return <code>true</code> if the page was successfully reverted to its
	 *         previous version
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

		// Pop off the top change list.
		final ChangeList changeList = (ChangeList)changeListStack.pop();
		if (changeList == null)
		{
			return false;
		}

		// Undo changes made to the previous version to get to this version.
		changeList.undo();

		// There is now one less version around.
		currentVersionNumber--;
		return true;
	}
}
