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
 * Converter that does nothing at all! Used for fallthrough; if really no converter is
 * found at all, this one is used.
 */
public final class NoopConverter implements Converter
{
    /**
     * noop; return value as was provided
     * @see Converter#convert(java.lang.Class, java.lang.Object)
     */
    public Object convert(Class type, Object value)
    {
        return value;
    }
}
