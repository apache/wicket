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
 * Interface for formatting values.
 */
public interface Formatter
{
    /**
     * Convert the specified input object into a locale-sensitive output string.
     * @param value The input object to be formatted
     * @param pattern The pattern is used for the conversion
     * @return object as a formatted string
     * @exception IllegalArgumentException if formatting cannot be performed successfully
     */
    public String format(Object value, String pattern) throws IllegalArgumentException;
}
