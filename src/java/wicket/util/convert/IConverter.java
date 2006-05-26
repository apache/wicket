/*
 * $Id: IConverter.java 5873 2006-05-25 22:45:15 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 22:45:15 +0000 (Thu, 25 May
 * 2006) $
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
package wicket.util.convert;

import java.io.Serializable;
import java.util.Locale;

/**
 * General purpose data type converter. An object that implements this interface
 * can convert objects from one class to another.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public interface IConverter extends Serializable
{
	/**
	 * Converts the given string value to class c.
	 * 
	 * @param value
	 *            The string value to convert
	 * @param locale
	 *            The locale used to convert the value
	 * @return The converted value
	 */
	Object convertToObject(String value, Locale locale);

	/**
	 * Converts the given value to a string.
	 * 
	 * @param value
	 *            The value to convert
	 * @param locale
	 *            TODO
	 * 
	 * @return The converted string value
	 */
	String convertToString(Object value, Locale locale);
}