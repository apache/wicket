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

/**
 * This folder subclass provides some type safety and extensibility.
 * @author Jonathan Locke
 */
public final class Folder extends File
{
	/** serialVersionUID */
	private static final long serialVersionUID = -2393525031297453147L;

	/**
     * Construct.
     * @param pathname path name
     */
    public Folder(final String pathname)
    {
        super(pathname);
    }

    /**
     * Construct.
     * @param parent parent
     * @param child child
     */
    public Folder(final String parent, final String child)
    {
        super(parent, child);
    }

    /**
     * Construct.
     * @param parent parent
     * @param child child
     */
    public Folder(final File parent, final String child)
    {
        super(parent, child);
    }

    /**
     * Construct.
     * @param uri folder uri
     */
    public Folder(final URI uri)
    {
        super(uri);
    }
}

///////////////////////////////// End of File /////////////////////////////////
