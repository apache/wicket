/*
 * $Id$ $Revision$ $Date:
 * 2005-10-02 03:06:33 -0700 (Sun, 02 Oct 2005) $
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

import java.io.FileFilter;
import java.net.URI;

/**
 * This folder subclass provides some type safety and extensibility for "files"
 * that hold other files.
 * 
 * @author Jonathan Locke
 */
public final class Folder extends File
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param pathname
	 *            path name
	 */
	public Folder(final String pathname)
	{
		super(pathname);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent
	 * @param child
	 *            child
	 */
	public Folder(final String parent, final String child)
	{
		super(parent, child);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent
	 * @param child
	 *            child
	 */
	public Folder(final File parent, final String child)
	{
		super(parent, child);
	}

	/**
	 * Constructor.
	 * 
	 * @param uri
	 *            folder uri
	 */
	public Folder(final URI uri)
	{
		super(uri);
	}

	/**
	 * File filter for folders
	 * 
	 * @author Jonathan Locke
	 */
	public static interface Filter
	{
		/**
		 * @param file
		 *            The file to test
		 * @return True if the file should be accepted
		 */
		public boolean accept(File file);
	}

	/**
	 * @param filter
	 *            Filename fiter
	 * @return Files
	 */
	public File[] getFiles(final Filter filter)
	{
		final java.io.File[] files = listFiles(new FileFilter()
		{
			/**
			 * @see java.io.FileFilter#accept(java.io.File)
			 */
			public boolean accept(java.io.File file)
			{
				return filter.accept(new File(file));
			}
		});
		final File[] wicketFiles = new File[files.length];
		for (int i = 0; i < files.length; i++)
		{
			wicketFiles[i] = new File(files[i]);
		}
		return wicketFiles;
	}
}
