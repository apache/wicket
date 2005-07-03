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
package wicket.extensions.markup.html.beanedit;

import wicket.util.lang.EnumeratedType;


/**
 * Model that knows about 'modes of operation'.
 * Comes default with MODE_READ_ONLY and MODE_EDIT, but user types can be defined.
 *
 * @author Eelco Hillenius
 */
public class EditMode extends EnumeratedType
{
	/** Mode that indicates the panel is in read only mode. */
	public static final EditMode READ_ONLY = new EditMode("read-only");

	/** Mode that indicates the panel is in edit mode. */
	public static final EditMode READ_WRITE = new EditMode("read-write");

	/**
	 * Construct a new edit mode type.
	 * @param name name of the edit mode
	 */
	public EditMode(String name)
	{
		super(name);
	}
}
