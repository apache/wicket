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


import java.net.URI;

import wicket.util.time.Time;
import wicket.util.watch.IChangeable;

/**
 * Simple extension of File that adds functionality.
 * @author Jonathan Locke
 */
public class File extends java.io.File implements IChangeable
{
	/** serialVersionUID */
	private static final long serialVersionUID = -1464216059997960924L;

	/**
     * Construct.
     * @param pathname path name
     */
    public File(final String pathname)
    {
        super(pathname);
    }

    /**
     * Construct.
     * @param parent parent
     * @param child child
     */
    public File(final String parent, final String child)
    {
        super(parent, child);
    }

    /**
     * Construct.
     * @param parent parent
     * @param child child
     */
    public File(final File parent, final String child)
    {
        super(parent, child);
    }

    /**
     * Construct.
     * @param uri file uri
     */
    public File(final URI uri)
    {
        super(uri);
    }

    /**
     * @return This file's lastModified() value as a Time object
     */
    public final Time lastModifiedTime()
    {
        return Time.milliseconds(lastModified());
    }
}

///////////////////////////////// End of File /////////////////////////////////
