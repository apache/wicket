/*
 * $Id$ $Revision:
 * 1.188 $ $Date$
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

package wicket;

/**
 * Thrown if metadata object is set to an object with the wrong type for
 * the MetaDataKey.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public class InvalidMetaDataTypeException extends WicketRuntimeException
{
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 695287693257632940L;

	/**
	 * Constructor.
	 * 
	 * @param message The message
	 */
	public InvalidMetaDataTypeException(final String message)
	{
		super(message);
	}
}
