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
package wicket.util.convert;

/**
 * Converts (formats) objects to a string.
 *
 * @author Eelco Hillenius
 */
public interface IStringConverter extends IConverter
{
	/**
	 * Converts the given value to a string.
	 * @param value the value to convert to a string
	 * @return to value as a string
	 */
	String toString(Object value);

	/**
	 * Converts the given value to an object.
	 * @param string the string to convert to an object
	 * @return the converted string
	 */
	Object valueOf(String string);
}
