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
package wicket.util.convert.converters;

import wicket.util.convert.Converter;

/**
 * General purpose data type converter. For this interface has the same signature of the
 * BeanUtils package, so it's easy to convert/ re-use converters.
 */
public interface LocaleConverter extends Converter
{ // TODO finalize javadoc
    /**
     * Convert the specified locale-sensitive input object into an output object of the
     * specified type.
     * @param type Data type to which this value should be converted
     * @param value The input value to be converted
     * @param pattern The user-defined pattern is used for the input object formatting.
     * @return converted object
     * @exception wicket.util.convert.ConversionException if conversion cannot be performed successfully
     */
    public Object convert(Class type, Object value, String pattern);
}
