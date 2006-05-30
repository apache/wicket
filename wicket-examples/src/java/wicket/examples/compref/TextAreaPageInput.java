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

/** Simple data class that acts as a model for the input fields. */
public class TextAreaPageInput implements Serializable
{
	/** some plain text. */
	public String text = "line 1\nline 2\nline 3";

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "text = '" + text + "'";
	}
}