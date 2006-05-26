/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:45:15 +0200 (vr, 26 mei 2006) $
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
package wicket.util.file;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import wicket.util.string.StringList;

/**
 * Mantains a list of folders as a path.
 * 
 * @author Jonathan Locke
 */
public final class Path implements IResourcePath
{
	/** The list of folders in the path */
	private final List<Folder> folders = new ArrayList<Folder>();

	/**
	 * Constructor
	 */
	public Path()
	{
	}

	/**
	 * Constructor
	 * 
	 * @param folder
	 *            A single folder to add to the path
	 */
	public Path(final Folder folder)
	{
		add(folder);
	}

	/**
	 * Constructor
	 * 
	 * @param folders
	 *            An array of folders to add to the path
	 */
	public Path(final Folder[] folders)
	{
		for (Folder element : folders)
		{
			add(element);
		}
	}

	/**
	 * @param folder
	 *            Folder to add to path
	 */
	public void add(final Folder folder)
	{
		if (!folder.exists())
		{
			throw new IllegalArgumentException("Folder " + folder + " does not exist");
		}

		folders.add(folder);
	}

	/**
	 * @param path
	 *            Folder to add to path
	 * @see wicket.util.file.IResourcePath#add(java.lang.String)
	 */
	public void add(final String path)
	{
		add(new Folder(path));
	}

	/**
	 * Looks for a given pathname along this path
	 * 
	 * @param pathname
	 *            The filename with possible path
	 * @return The url located on the path
	 */
	public URL find(final String pathname)
	{
		for (Folder folder : folders)
		{
			final File file = new File(folder, pathname);

			if (file.exists())
			{
				try
				{
					return file.toURI().toURL();
				}
				catch (MalformedURLException ex)
				{
					// ignore
				}
			}
		}

		return null;
	}

	/**
	 * @return Returns the folders.
	 */
	public List<Folder> getFolders()
	{
		return folders;
	}

	/**
	 * @return Number of folders on the path.
	 */
	public int size()
	{
		return folders.size();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[folders = " + StringList.valueOf(folders) + "]";
	}
}
