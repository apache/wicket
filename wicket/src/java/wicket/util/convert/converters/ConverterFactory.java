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
import wicket.util.convert.IConverter;
import wicket.util.convert.IConverterFactory;
import wicket.util.convert.converters.BooleanConverter;
import wicket.util.convert.converters.CharacterConverter;
import wicket.util.convert.converters.IntegerConverter;

/**
 * The default, non localized implementation of
 * {@link wicket.util.convert.IConverterFactory}.
 *
 * @author Eelco Hillenius
 */
public final class ConverterFactory implements IConverterFactory
{
	/**
	 * Construct.
	 */
	public ConverterFactory()
	{
	}

	/**
	 * @see wicket.util.convert.IConverterFactory#newConverter()
	 */
	public IConverter newConverter()
	{
		Converter converter = new Converter();
      BooleanConverter booleanConverter = new BooleanConverter();
		converter.add(booleanConverter, Boolean.TYPE);
      converter.add(booleanConverter, Boolean.class);
      ByteConverter byteConverter = new ByteConverter();
		converter.add(byteConverter, Byte.TYPE);
      converter.add(byteConverter, Byte.class);
      CharacterConverter characterConverter = new CharacterConverter();
		converter.add(characterConverter, Character.TYPE);
      converter.add(characterConverter, Character.class);
      DoubleConverter doubleConverter = new DoubleConverter();
		converter.add(doubleConverter, Double.TYPE);
      converter.add(doubleConverter, Double.class);
      FloatConverter floatConverter = new FloatConverter();
		converter.add(floatConverter, Float.TYPE);
      converter.add(floatConverter, Float.class);
      IntegerConverter integerConverter = new IntegerConverter();
		converter.add(integerConverter, Integer.TYPE);
      converter.add(integerConverter, Integer.class);
      LongConverter longConverter = new LongConverter();
		converter.add(longConverter, Long.TYPE);
      converter.add(longConverter, Long.class);
      ShortConverter shortConverter = new ShortConverter();
		converter.add(shortConverter, Short.TYPE);
      converter.add(shortConverter, Short.class);
		return converter;
	}
}