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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ognl.OgnlOps;

/**
 * Default converter implementation.
 *
 * @author Eelco Hillenius
 */
public final class Converter implements IConverter, IStringConverter, ILocalizable
{
	/** maps classes to converters. */
	private final Map converters = new HashMap();

	/** the current locale. */
	private Locale locale = null;

	/**
	 * converter that is to be used when no registered converter is found.
	 */
	private IConverter fallThroughConverter = new IConverter()
	{
		/**
		 * Converts the given value object to class c using OgnlOps.
		 * @see wicket.util.convert.IConverter#convert(java.lang.Object, java.lang.Class)
		 */
		public Object convert(Object value, Class c)
		{
			return OgnlOps.convertValue(value, c);
		}
	};
	
	/**
	 * Construct.
	 */
	public Converter()
	{
		super();
	}

	/**
	 * Converts the given value to class c.
	 * @param value the value to convert
	 * @param c the class to convert to
	 * @return the converted value
	 *
	 * @see wicket.util.convert.IConverter#convert(java.lang.Object, java.lang.Class)
	 */
	public Object convert(Object value, Class c)
	{
		if(value == null)
		{
			return null;
		}
		if(c == null)
		{
			throw new IllegalArgumentException(
					"parameter c(lass( must be not-null");
		}
		IConverter converter = get(c);
		if(converter == null)
		{
			converter = fallThroughConverter;
		}
		if(converter instanceof ILocalizable)
		{
			((ILocalizable)converter).setLocale(locale);
		}
		try
		{
			return converter.convert(value, c);
		}
      catch (ConversionException e)
      {
          throw e.setConverter(converter).setLocale(locale)
          	.setTargetType(c).setTriedValue(value);
      }
      catch (Exception e)
      {
          throw new ConversionException(e).setConverter(converter)
          	.setLocale(locale).setTargetType(c).setTriedValue(value);
      }
	}

	/**
	 * @see wicket.util.convert.IStringConverter#valueOf(java.lang.String)
	 */
	public Object valueOf(String string)
	{
		return convert(string, String.class);
	}

	/**
	 * @see wicket.util.convert.IStringConverter#toString(java.lang.Object)
	 */
	public String toString(Object value)
	{
		if(value == null)
		{
			return null;
		}
		Class c = value.getClass();
		IConverter converter = get(c);
		if(converter == null)
		{
			converter = fallThroughConverter;
		}
		if(converter instanceof ILocalizable)
		{
			((ILocalizable)converter).setLocale(locale);
		}
		try
		{
			return (String)converter.convert(value, String.class);
		}
      catch (ConversionException e)
      {
          throw e.setConverter(converter).setLocale(locale)
          	.setTargetType(c).setTriedValue(value);
      }
      catch (Exception e)
      {
          throw new ConversionException(e).setConverter(converter)
          	.setLocale(locale).setTargetType(c).setTriedValue(value);
      }
	}

	/**
	 * @see wicket.util.convert.ILocalizable#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * Registers a converter for use with class c.
	 * @param converter the converter to add
	 * @param c the class for which the converter should be used
	 * @return the previous registered converter for class c or null if
	 * none was registered yet for class c
	 */
	public IConverter add(IConverter converter, Class c)
	{
		if(converter == null || c == null)
		{
			throw new IllegalArgumentException(
					"parameters converter and c(lass( must be not-null");
		}
		IConverter old = (IConverter)converters.put(c, converter);
		return old;
	}

	/**
	 * Removes the converter currently registered for class c.
	 * @param c the class for which the converter registration should be removed
	 * @return the converter that was registered for class c before removal or
	 * null if none was registered
	 */
	public IConverter remove(Class c)
	{
		return (IConverter)converters.remove(c);
	}

	/**
	 * Gets the converter that is registered for class c.
	 * @param c the class to get the converter for
	 * @return the converter that is registered for class c or null if no
	 *         converter was registered for class c
	 */
	public IConverter get(Class c)
	{
		return (IConverter)converters.get(c);
	}

	/**
	 * Removes all registers converters.
	 */
	public void clear()
	{
		converters.clear();
	}

	/**
	 * Gets the converter that is to be used when no registered
	 * 	converter is found.
	 * @return the converter that is to be used when no registered
	 * 	converter is found
	 */
	protected final IConverter getFallThroughConverter()
	{
		return fallThroughConverter;
	}

	/**
	 * Sets the converter that is to be used when no registered
	 * 	converter is found.
	 * @param fallThroughConverter the converter that is to be used
	 * 	when no registered converter is found
	 */
	protected final void setFallThroughConverter(IConverter fallThroughConverter)
	{
		this.fallThroughConverter = fallThroughConverter;
	}
}