/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.file;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wicket.util.string.StringList;

/**
 * Mantains a list of folders as a path.
 * 
 * @author Jonathan Locke
 */
public final class Path
{
    /** The list of folders in the path */
    private final List folders = new ArrayList();

    /**
     * Constructor
     */
    public Path()
    {
    }

    /**
     * Constructor
     * @param folder A single folder to add to the path
     */
    public Path(final Folder folder)
    {
        add(folder);
    }

    /**
     * Constructor
     * @param folders An array of folders to add to the path
     */
    public Path(final Folder[] folders)
    {
        for (int i = 0; i < folders.length; i++)
        {
            add(folders[i]);
        }
    }

    /**
     * @param folder Folder to add to path
     * @return The path, for invocation chaining
     */
    public Path add(final Folder folder)
    {
        if (!folder.exists())
        {
            throw new IllegalArgumentException("Folder " + folder + " does not exist");
        }

        folders.add(folder);

        return this;
    }

    /**
     * Looks for a given pathname along this path
     * @param pathname The filename with possible path
     * @return The file located on the path
     */
    public File find(final String pathname)
    {
        for (final Iterator iterator = folders.iterator(); iterator.hasNext();)
        {
            final Folder folder = (Folder) iterator.next();
            final File file = new File(folder, pathname);

            if (file.exists())
            {
                return file;
            }
        }

        return null;
    }

    /**
     * @return Returns the folders.
     */
    public List getFolders()
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
    public String toString()
    {
        return "[folders = " + StringList.valueOf(folders) + "]";
    }
}

///////////////////////////////// End of File /////////////////////////////////
