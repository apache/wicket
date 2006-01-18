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
package wicket.version;

import java.io.Serializable;

import wicket.Component;
import wicket.Page;
import wicket.version.undo.Change;

/**
 * Interface to code that manages versions of a Page. Initially a page has a
 * version number of 0, indicating that it is in its original state. When one or
 * more changes are made to the page, we arrive at version 1.
 * <p>
 * During a request cycle, just before a change is about to occur,
 * beginVersion() is called, followed by one or more calls to componentAdded(),
 * componentRemoved() or componentModelChanging(). If beginVersion() is called
 * by the framework during a given request cycle, a balancing endVersion() call
 * will occur at the end of the request cycle. However, if no changes occur to a
 * page during a request cycle, none of these methods will be called.
 * <p>
 * Once version information has been added to a version manager, versions can be
 * retrieved by number using getVersion(int). Since version 0 is the first
 * version of a page, calling getVersion(0) will retrieve that version.
 * <p>
 * The current version number (the number of the newest available version) of a
 * page can be retrieved by calling getCurrentVersionNumber().
 * 
 * @author Jonathan Locke
 */
public interface IPageVersionManager extends Serializable
{
	/**
	 * Called when changes are immediately impending to the Page being managed.
	 * The changes to the page between the call to this method and the call to
	 * endVersion() create a new version of the page.
	 * <p>
	 * In requests where a Page is not changed at all, beginVersion will never
	 * be called, nor will any of the other methods in this interface.
	 */
	void beginVersion();

	/**
	 * Indicates that the given component was added.
	 * 
	 * @param component
	 *            The component that was added.
	 */
	void componentAdded(Component component);

	/**
	 * Indicates that the model for the given component is about to change.
	 * 
	 * @param component
	 *            The component whose model is about to change
	 */
	void componentModelChanging(Component component);

	/**
	 * Indicates an internal state for the given component is about to change.
	 * 
	 * @param change
	 *            The change which represents the internal state
	 */
	void componentStateChanging(Change change);

	/**
	 * Indicates that the given component was removed.
	 * 
	 * @param component
	 *            The component that was removed.
	 */
	void componentRemoved(Component component);

	/**
	 * Called when changes to the page have ended.
	 * 
	 * @see IPageVersionManager#beginVersion()
	 */
	void endVersion();
	
	/**
	 * Expires oldest version
	 */
	void expireOldestVersion();

	/**
	 * Retrieves a given Page version.
	 * 
	 * @param versionNumber
	 *            The version of the page to get
	 * @return The page or null if the version requested is not available
	 */
	Page getVersion(int versionNumber);

	/**
	 * @return The number of versions stored in this version manager
	 */
	int getVersions();

	/**
	 * @return Returns the current (newest) version number available through
	 *         this version manager.
	 */
	int getCurrentVersionNumber();
}
