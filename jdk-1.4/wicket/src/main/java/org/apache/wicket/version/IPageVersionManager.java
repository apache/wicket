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
package org.apache.wicket.version;

import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.apache.wicket.Page;
import org.apache.wicket.version.undo.Change;

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
public interface IPageVersionManager extends IClusterable
{
	/**
	 * Called when changes are immediately impending to the Page being managed.
	 * The changes to the page between the call to this method and the call to
	 * endVersion() create a new version of the page.
	 * <p>
	 * In requests where a Page is not changed at all, beginVersion will never
	 * be called, nor will any of the other methods in this interface.
	 * 
	 * @param mergeVersion
	 * 			  If this is set the version that was created is merged with the previous one. 
	 */
	void beginVersion(boolean mergeVersion);

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
	 * @param mergeVersion
	 * 			  If this is set the version that was created is merged with the previous one. 
	 * 
	 * @see IPageVersionManager#beginVersion(boolean)
	 */
	void endVersion(boolean mergeVersion);
	
	/**
	 * Expires oldest version
	 */
	void expireOldestVersion();

	/**
	 * Retrieves a given Page version.
	 * This method does not take use the ajax versions.
	 * 
	 * @param versionNumber
	 *            The version of the page to get
	 * @return The page or null if the version requested is not available
	 */
	Page getVersion(int versionNumber);


	/**
	 * This method rollbacks the page the number of versions specified
	 * Including the ajax versions.
	 * 
	 * @param numberOfVersions  the number of versions to rollback
	 * @return the rolled-back page
	 */
	Page rollbackPage(int numberOfVersions);

	/**
	 * @return The number of versions stored in this version manager
	 */
	int getVersions();

	/**
	 * @return Returns the current (newest) version number available through
	 *         this version manager.
	 */
	int getCurrentVersionNumber();

	/**
	 * @return Returns the current ajax version number.
	 */
	int getAjaxVersionNumber();

	/**
	 * Call this method when the current (ajax) request
	 * shouldn't merge the changes that are happening to the page 
	 * with the previous version. This is for example needed
	 * when you want to redirect to this page in an ajax request
	 * and then you do want to version normally.. 
	 * 
	 * This method should only be called if the beginVersion was called with true!
	 */
	void ignoreVersionMerge();
}
