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
 * An interface that manages versions of a <code>Page</code>. Initially a page has a version
 * number of 0, indicating that it is in its original state. When one or more changes are made to
 * the page, we arrive at version 1.
 * <p>
 * During a <code>RequestCycle</code>, just before a change is about to occur, the
 * <code>beginVersion</code> method is called, followed by one or more calls to
 * <code>componentAdded</code>, <code>componentRemoved</code> or
 * <code>componentModelChanging</code>. If <code>beginVersion</code> is called by the framework
 * during a given request cycle, a balancing call to <code>endVersion</code> will occur at the end
 * of the request cycle. However, if no changes occur to a page during a request cycle, none of
 * these methods will be called.
 * <p>
 * Once version information has been added to a page version manager (<code>IPageVersionManager</code>),
 * versions can be retrieved by number using the <code>getVersion(int)</code> method. Since
 * version 0 is the first version of a page, calling <code>getVersion(0)</code> will retrieve that
 * version.
 * <p>
 * The current version number of a page (that is, the number of the newest available version) can be
 * retrieved by calling <code>getCurrentVersionNumber</code>.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public interface IPageVersionManager extends IClusterable
{
	/**
	 * Called when changes are immediately impending to the <code>Page</code> being managed. The
	 * changes to the page between the call to this method and the call to <code>endVersion</code>
	 * create a new version of the page.
	 * <p>
	 * In requests where a page is not changed at all, <code>beginVersion</code> will never be
	 * called, nor will any of the other methods in this interface.
	 * 
	 * @param mergeVersion
	 *            If this is set, the version that was created is merged with the previous one.
	 */
	void beginVersion(boolean mergeVersion);

	/**
	 * Indicates that the given <code>Component</code> was added.
	 * 
	 * @param component
	 *            the <code>Component</code> that was added
	 */
	void componentAdded(Component component);

	/**
	 * Indicates that the model for the given <code>Component</code> is about to change.
	 * 
	 * @param component
	 *            the <code>Component</code> whose model is about to change
	 */
	void componentModelChanging(Component component);

	/**
	 * Indicates an internal state for the given <code>Component</code> is about to change.
	 * 
	 * @param change
	 *            the <code>Change</code> which represents the internal state
	 */
	void componentStateChanging(Change change);

	/**
	 * Indicates that the given <code>Component</code> was removed.
	 * 
	 * @param component
	 *            the <code>Component</code> that was removed
	 */
	void componentRemoved(Component component);

	/**
	 * Called when changes to the <code>Page</code> have ended.
	 * 
	 * @param mergeVersion
	 *            If this is set, the version that was created is merged with the previous one.
	 * @see IPageVersionManager#beginVersion(boolean)
	 */
	void endVersion(boolean mergeVersion);

	/**
	 * Expires oldest version in this page version manager.
	 */
	void expireOldestVersion();

	/**
	 * Retrieves a given <code>Page</code> version. This method does not include the Ajax
	 * versions.
	 * 
	 * @param versionNumber
	 *            the version of the page to get
	 * @return the <code>Page</code>, or <code>null</code> if the version requested is not
	 *         available
	 */
	Page getVersion(int versionNumber);


	/**
	 * Rolls back the <code>Page</code> by the number of versions specified, including the Ajax
	 * versions.
	 * 
	 * @param numberOfVersions
	 *            the number of versions to roll back
	 * @return the rolled-back <code>Page</code>
	 */
	Page rollbackPage(int numberOfVersions);

	/**
	 * Retrieves the number of versions stored in this page version manager.
	 * 
	 * @return the number of versions stored in this <code>IPageVersionManager</code>
	 */
	int getVersions();

	/**
	 * Retrieves the newest version number available in this page version manager.
	 * 
	 * @return the current (newest) version number available in this
	 *         <code>IPageVersionManager</code>
	 */
	int getCurrentVersionNumber();

	/**
	 * Retrieves the current Ajax version number.
	 * 
	 * @return the current Ajax version number.
	 */
	int getAjaxVersionNumber();

	/**
	 * Call this method if the current Ajax request shouldn't merge changes that are happening to
	 * the <code>Page</code> with the previous version. This is needed, for example, when you want
	 * to redirect to this page in an Ajax request, and then you want to version normally.
	 * <p>
	 * This method should only be called if the <code>beginVersion</code> method was called with
	 * <code>true</code>!
	 * 
	 * @see IPageVersionManager#beginVersion(boolean)
	 */
	void ignoreVersionMerge();
}
