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
 * Interface to code that manages versions of a Page.
 * 
 * @author Jonathan Locke
 */
public interface IPageVersionManager
{
	/**
	 * A PageVersionManager that does nothing
	 */
	public static final IPageVersionManager NULL = new IPageVersionManager()
	{
		public void beginVersion()
		{
		}

		public void componentAdded(Component component)
		{
		}

		public void componentModelChangeImpending(Component component)
		{
		}

		public void componentRemoved(Component component)
		{
		}

		public void endVersion()
		{
		}

		public Page getVersion(int version)
		{
			return null;
		}

		public int getVersion()
		{
			return 0;
		}
	};

	/**
	 * Called when changes are immediately impending to the Page being managed
	 * by this version manager. In requests where the Page is not changed at
	 * all, none of the methods in this interface will ever be called.
	 */
	public void beginVersion();

	/**
	 * Indicates that the given component was added.
	 * 
	 * @param component
	 *            The component that was added.
	 */
	public void componentAdded(Component component);

	/**
	 * Indicates that the model for the given component is about to change.
	 * 
	 * @param component
	 *            The component whose model is about to change
	 */
	public void componentModelChangeImpending(Component component);

	/**
	 * Indicates that the given component was removed.
	 * 
	 * @param component
	 *            The component that was removed.
	 */
	public void componentRemoved(Component component);

	/**
	 * Called when changes to the page have ended.
	 */
	public void endVersion();

	/**
	 * Retrieves a page of the given version
	 * 
	 * @param version
	 *            The version to get
	 * @return The page
	 */
	public Page getVersion(int version);

	/**
	 * @return Returns the current (most recent) version being managed by this
	 *         version manager.
	 */
	public int getVersion();
}
