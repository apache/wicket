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

import java.util.ArrayList;
import java.util.List;

import wicket.Component;
import wicket.IPageVersionManager;
import wicket.Page;

/**
 * A version manager implemented by recording component changes as undo records
 * which can later be reversed to get back to a given version of the Page being
 * managed.
 * 
 * @author Jonathan Locke
 */
public class UndoPageVersionManager implements IPageVersionManager
{
	/** The page being managed */
	private final Page page;
	
	/** List of versions */
	private final List versions = new ArrayList();
	
	/** The current version */
	private Version version;

	/**
	 * Constructor
	 * 
	 * @param page
	 *            The page that we're versioning
	 */
	public UndoPageVersionManager(final Page page)
	{
		this.page = page;
	}

	/**
	 * @see wicket.IPageVersionManager#beginVersion()
	 */
	public void beginVersion()
	{
		version = new Version();
	}

	/**
	 * @see wicket.IPageVersionManager#componentAdded(wicket.Component)
	 */
	public void componentAdded(Component component)
	{
		version.componentAdded(component);
	}

	/**
	 * @see wicket.IPageVersionManager#componentModelChangeImpending(wicket.Component)
	 */
	public void componentModelChangeImpending(Component component)
	{
		version.componentModelChangeImpending(component);
	}

	/**
	 * @see wicket.IPageVersionManager#componentRemoved(wicket.Component)
	 */
	public void componentRemoved(Component component)
	{
		version.componentRemoved(component);
	}

	/**
	 * @see wicket.IPageVersionManager#endVersion()
	 */
	public void endVersion()
	{
		versions.add(version);
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
		return versions.size() - 1;
	}
}
