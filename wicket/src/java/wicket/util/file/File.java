/*
 * $Id$ $Revision$
 * $Date$
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

import java.net.URI;

import wicket.util.time.Time;
import wicket.util.watch.IModifiable;

/**
 * Simple extension of File that adds an implementation of IModifiable for
 * files. This allows the ModificationWatcher class to watch files for
 * modification. The IModifiable.lastModifiedTime() method also returns a Time
 * object with a more convenient API than either Date or a value in
 * milliseconds.
 * 
 * @author Jonathan Locke
 */
public class File extends java.io.File implements IModifiable
{
	/** serialVersionUID */
	private static final long serialVersionUID = -1464216059997960924L;

	/**
	 * Constructor.
	 * 
	 * @param pathname
	 *            path name
	 */
	public File(final String pathname)
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
	public File(final String parent, final String child)
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
	public File(final File parent, final String child)
	{
		super(parent, child);
	}

	/**
	 * Constructor.
	 * 
	 * @param uri
	 *            file uri
	 */
	public File(final URI uri)
	{
		super(uri);
	}

	/**
	 * Returns a Time object representing the most recent time this file was
	 * modified.
	 * 
	 * @return This file's lastModified() value as a Time object
	 */
	public final Time lastModifiedTime()
	{
		return Time.milliseconds(lastModified());
	}
}
