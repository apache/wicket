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
import wicket.IPageRevisionManager;
import wicket.Page;

/**
 * A version manager implemented by recording component changes as undo records
 * which can later be reversed to get back to a given revision of the Page being
 * managed.
 * 
 * @author Jonathan Locke
 */
public class UndoPageRevisionManager implements IPageRevisionManager
{
	/** The page being managed */
	private final Page page;

	/** List of versions */
	private final Map versions;

	/** The current version */
	private Revision version;

	/** The next available version number. */
	private int nextVersionNumber = 0;

	/**
	 * Constructor
	 * 
	 * @param page
	 *            The page that we're versioning
	 * @param maxVersions
	 *            The maximum number of versions to maintain before expiring the
	 *            old versions
	 */
	public UndoPageRevisionManager(final Page page, final int maxVersions)
	{
		this.page = page;
		this.versions = new LinkedHashMap()
		{
			protected boolean removeEldestEntry(Map.Entry ignored)
			{
				// Remove oldest entry if there are more than maxVersions
				// entries
				return size() > maxVersions;
			}
		};
	}

	/**
	 * @see wicket.IPageRevisionManager#beginRevision()
	 */
	public void beginRevision()
	{
		version = new Revision();
		nextVersionNumber++;
	}

	/**
	 * @see wicket.IPageRevisionManager#componentAdded(wicket.Component)
	 */
	public void componentAdded(Component component)
	{
		version.componentAdded(component);
	}

	/**
	 * @see wicket.IPageRevisionManager#componentModelChangeImpending(wicket.Component)
	 */
	public void componentModelChangeImpending(Component component)
	{
		version.componentModelChangeImpending(component);
	}

	/**
	 * @see wicket.IPageRevisionManager#componentRemoved(wicket.Component)
	 */
	public void componentRemoved(Component component)
	{
		version.componentRemoved(component);
	}

	/**
	 * @see wicket.IPageRevisionManager#endRevision()
	 */
	public void endRevision()
	{
		versions.put(new Integer(getNewestRevisionNumber()), version);
	}

	/**
	 * @see wicket.IPageRevisionManager#getRevision(int)
	 */
	public Page getRevision(int versionNumber)
	{
		// Get version for version number
		final Revision version = (Revision)versions.get(new Integer(versionNumber));

		// Found it?
		if (version != null)
		{
			// Undo each version from last to first up to our version number
			while (getNewestRevisionNumber() > versionNumber)
			{
				nextVersionNumber--;
				final Integer key = new Integer(nextVersionNumber);
				final Revision versionToUndo = (Revision)versions.get(key);
				versionToUndo.undo();
				versions.remove(key);
			}

			// Return modified page
			return page;
		}

		return null;
	}

	/**
	 * @see wicket.IPageRevisionManager#getNewestRevisionNumber()
	 */
	public int getNewestRevisionNumber()
	{
		return nextVersionNumber - 1;
	}
}
