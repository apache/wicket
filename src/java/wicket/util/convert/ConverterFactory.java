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

import wicket.util.convert.converters.BooleanConverter;
import wicket.util.convert.converters.ByteConverter;
import wicket.util.convert.converters.CharacterConverter;
import wicket.util.convert.converters.DoubleConverter;
import wicket.util.convert.converters.FloatConverter;
import wicket.util.convert.converters.IntegerConverter;
import wicket.util.convert.converters.LongConverter;
import wicket.util.convert.converters.ShortConverter;
import wicket.util.convert.converters.StringConverter;

/**
 * The default, non localized implementation of
 * {@link wicket.util.convert.IConverterFactory}.
 *
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class ConverterFactory implements IConverterFactory
{
    /**
     * @see wicket.util.convert.IConverterFactory#newConverter()
     */
    public IConverter newConverter()
    {
        Converter converter = new Converter();
        converter.set(Boolean.TYPE, new BooleanConverter());
        converter.set(Boolean.class, new BooleanConverter());
        converter.set(Byte.TYPE, new ByteConverter());
        converter.set(Byte.class, new ByteConverter());
        converter.set(Character.TYPE, new CharacterConverter());
        converter.set(Character.class, new CharacterConverter());
        converter.set(Double.TYPE, new DoubleConverter());
        converter.set(Double.class, new DoubleConverter());
        converter.set(Float.TYPE, new FloatConverter());
        converter.set(Float.class, new FloatConverter());
        converter.set(Integer.TYPE, new IntegerConverter());
        converter.set(Integer.class, new IntegerConverter());
        converter.set(Long.TYPE, new LongConverter());
        converter.set(Long.class, new LongConverter());
        converter.set(Short.TYPE, new ShortConverter());
        converter.set(Short.class, new ShortConverter());
        converter.set(String.class, new StringConverter());
        return converter;
    }
}