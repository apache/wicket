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
 * General purpose data type converter.
 *
 * @author Eelco Hillenius
 */
public interface IConverter
{
	/**
	 * if this/ null is passed to the convert method as the class argument,
	 * it indicates that the default object conversion should be applied.
	 */
	public static final Class CONVERT_TO_DEFAULT_TYPE = null;

	/**
	 * Converts the given value to class c.
	 * @param value the value to convert
	 * @param c the class to convert to. If this argument is null, it could be used
	 * as an indicator that the converter's default conversion should be applied.
	 * @return the converted value
	 */
	public Object convert(Object value, Class c);
}
