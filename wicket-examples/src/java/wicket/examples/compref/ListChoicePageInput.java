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
package wicket.examples.compref;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/** Simple data class that acts as a model for the input fields. */
public class ListChoicePageInput implements Serializable
{
	/** available sites for selection. */
	static final List SITES = Arrays.asList(new String[] { "The Server Side", "Java Lobby",
			"Java.Net" });

	/** the selected site. */
	public String site = (String)SITES.get(0);

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "site = '" + site + "'";
	}
}