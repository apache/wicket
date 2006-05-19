/*
 * $Id$ $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.beanedit;

import wicket.util.lang.EnumeratedType;


/**
 * Model that knows about 'modes of operation'. Comes default with
 * MODE_READ_ONLY, MODE_EDIT and INVISIBLE, but user types can be defined.
 * 
 * @author Eelco Hillenius
 */
public class EditMode extends EnumeratedType
{
	private static final long serialVersionUID = 1L;

	/** Mode that indicates the subject or field is in read only mode. */
	public static final EditMode READ_ONLY = new EditMode("read-only");

	/** Mode that indicates the subject is in edit mode. */
	public static final EditMode READ_WRITE = new EditMode("read-write");

	/** Mode that indicates the subject is not visible. */
	public static final EditMode INVISIBLE = new EditMode("invisible");

	/**
	 * Construct a new edit mode type.
	 * 
	 * @param name
	 *            name of the edit mode
	 */
	public EditMode(String name)
	{
		super(name);
	}
}
