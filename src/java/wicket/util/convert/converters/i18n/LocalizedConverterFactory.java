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
package wicket.util.convert.converters.i18n;

import wicket.util.convert.Converter;
import wicket.util.convert.IConverter;
import wicket.util.convert.IConverterFactory;
import wicket.util.convert.converters.BooleanConverter;
import wicket.util.convert.converters.CharacterConverter;
import wicket.util.convert.converters.IntegerConverter;

/**
 * Converter factory that is usefull in a localized environment.
 *
 * @author Eelco Hillenius
 */
public final class LocalizedConverterFactory implements IConverterFactory
{
	/**
	 * Construct.
	 */
	public LocalizedConverterFactory()
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
      ByteLocaleConverter byteLocaleConverter = new ByteLocaleConverter();
		converter.add(byteLocaleConverter, Byte.TYPE);
      converter.add(byteLocaleConverter, Byte.class);
      CharacterConverter characterConverter = new CharacterConverter();
		converter.add(characterConverter, Character.TYPE);
      converter.add(characterConverter, Character.class);
      DoubleLocaleConverter doubleLocaleConverter = new DoubleLocaleConverter();
		converter.add(doubleLocaleConverter, Double.TYPE);
      converter.add(doubleLocaleConverter, Double.class);
      FloatLocaleConverter floatLocaleConverter = new FloatLocaleConverter();
		converter.add(floatLocaleConverter, Float.TYPE);
      converter.add(floatLocaleConverter, Float.class);
      IntegerConverter integerConverter = new IntegerConverter();
		converter.add(integerConverter, Integer.TYPE);
      converter.add(integerConverter, Integer.class);
      LongLocaleConverter longLocaleConverter = new LongLocaleConverter();
		converter.add(longLocaleConverter, Long.TYPE);
      converter.add(longLocaleConverter, Long.class);
      ShortLocaleConverter shortLocaleConverter = new ShortLocaleConverter();
		converter.add(shortLocaleConverter, Short.TYPE);
      converter.add(shortLocaleConverter, Short.class);
      DateLocaleConverter dateLocaleConverter = new DateLocaleConverter();
		converter.add(dateLocaleConverter, java.util.Date.class);
      converter.add(dateLocaleConverter, java.sql.Date.class);
      converter.add(dateLocaleConverter, java.sql.Timestamp.class);
		return converter;
	}

}
