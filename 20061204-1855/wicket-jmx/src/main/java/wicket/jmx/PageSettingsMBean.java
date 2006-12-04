/*
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
package wicket.jmx;

import wicket.markup.html.WebPage;

/**
 * Page settings.
 * 
 * @author eelcohillenius
 */
public interface PageSettingsMBean
{
	/**
	 * Gets whether Wicket should try to support opening multiple windows for
	 * the same session transparently. If this is true - the default setting -,
	 * Wicket tries to detect whether a new window was opened by a user (e.g. in
	 * Internet Explorer by pressing ctrl+n or ctrl+click on a link), and if it
	 * detects that, it creates a new page map for that window on the fly. As a
	 * page map represents the 'history' of one window, each window will then
	 * have their own history. If two windows would share the same page map, the
	 * non-bookmarkable links on one window could refer to stale state after
	 * working a while in the other window.
	 * <p>
	 * <strong> Currently, Wicket trying to do this is a best effort that is not
	 * completely fail safe. When the client does not support cookies, support
	 * gets tricky and incomplete. See {@link WebPage}'s internals for the
	 * implementation. </strong>
	 * </p>
	 * 
	 * @return Whether Wicket should try to support multiple windows
	 *         transparently
	 */
	boolean getAutomaticMultiWindowSupport();

	/**
	 * @return Returns the maxPageVersions.
	 */
	int getMaxPageVersions();

	/**
	 * @return Returns the pagesVersionedByDefault.
	 */
	boolean getVersionPagesByDefault();

	/**
	 * Sets whether Wicket should try to support opening multiple windows for
	 * the same session transparently. If this is true - the default setting -,
	 * Wicket tries to detect whether a new window was opened by a user (e.g. in
	 * Internet Explorer by pressing ctrl+n or ctrl+click on a link), and if it
	 * detects that, it creates a new page map for that window on the fly. As a
	 * page map represents the 'history' of one window, each window will then
	 * have their own history. If two windows would share the same page map, the
	 * non-bookmarkable links on one window could refer to stale state after
	 * working a while in the other window.
	 * <p>
	 * <strong> Currently, Wicket trying to do this is a best effort that is not
	 * completely fail safe. When the client does not support cookies, support
	 * gets tricky and incomplete. See {@link WebPage}'s internals for the
	 * implementation. </strong>
	 * </p>
	 * 
	 * @param automaticMultiWindowSupport
	 *            Whether Wicket should try to support multiple windows
	 *            transparently
	 */
	void setAutomaticMultiWindowSupport(boolean automaticMultiWindowSupport);

	/**
	 * @param maxPageVersions
	 *            The maxPageVersion to set.
	 */
	void setMaxPageVersions(int maxPageVersions);

	/**
	 * @param pagesVersionedByDefault
	 *            The pagesVersionedByDefault to set.
	 */
	void setVersionPagesByDefault(boolean pagesVersionedByDefault);
}
