/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.convert;

import wicket.Component;

/**
 * Adapter class to simplify implementing custom {@link IConverter}s. If the
 * requested type to convert to is a string type (or more precice, assignable
 * from {@link CharSequence}), this converter will delegate type conversion to
 * {@link #toString(Object)}. Otherwise, it will delegate type conversion to
 * {@link #toObject(String)} using the object's {@link Object#toString()} value
 * to convert it to a string first (or passing in null in case the value is
 * null).
 * <p>
 * Note, this class is specifically meant for providing custom converters per
 * component by overriding {@link Component#getConverter()}. It is less usefull
 * for application scoped converters; registering {@link ITypeConverter}s with
 * an instance of {@link Converter} is a better choice for that.
 * </p>
 * <p>
 * <strong>WARNING. Due to a current limitation as a result of how
 * {@link IConverter} works, classes that extend this adapter will not be much
 * use with string values. If you want to use a custom converter for string
 * values, consider wrapping the values in another class so that conversion will
 * be triggered. See the form input example of wicket-examples for how this can
 * be done. </strong>
 * </p>
 * <p>
 * An example of the use of this class is the following:
 * 
 * <pre>
 * add(new TextField(&quot;urlProperty&quot;, URL.class)
 * {
 * 	public IConverter getConverter()
 * 	{
 * 		return new SimpleConverterAdapter()
 * 		{
 * 			public String toString(Object value)
 * 			{
 * 				return value != null ? value.toString() : null;
 * 			}
 * 
 * 			public Object toObject(String value)
 * 			{
 * 				try
 * 				{
 * 					return new URL(value.toString());
 * 				}
 * 				catch (MalformedURLException e)
 * 				{
 * 					throw new ConversionException(&quot;'&quot; + value + &quot;' is not a valid URL&quot;);
 * 				}
 * 			}
 * 		};
 * 	}
 * });
 * </pre>
 * 
 * </p>
 * 
 * @author Eelco Hillenius
 */
public abstract class SimpleConverterAdapter extends LocalizableAdapter implements IConverter
{
	private static final long serialVersionUID = 1L;

	/**
	 * If class c is a string type (or more precise, assignable from
	 * {@link CharSequence}), this method will delegate type conversion to
	 * {@link #toString(Object)}. Otherwise, it will delegate type conversion
	 * to {@link #toObject(String)} using the object's {@link Object#toString()}
	 * value to convert it to a string first (or passing in null in case the
	 * value is null).
	 * 
	 * @see wicket.util.convert.IConverter#convert(java.lang.Object,
	 *      java.lang.Class)
	 */
	public final Object convert(Object value, Class c)
	{
		if (String.class.isAssignableFrom(c))
		{
			return toString(value);
		}
		else if (value instanceof CharSequence)
		{
			return toObject((value instanceof String) ? (String)value : value.toString());
		}
		else if (value != null && (!c.isAssignableFrom(value.getClass())))
		{
			throw new IllegalArgumentException("unable to convert " + value + " to type " + c);
		}
		return value; // null
	}

	/**
	 * Convert the given value to a string.
	 * 
	 * @param value
	 *            The value to convert, may be null
	 * @return The value as a string
	 */
	public abstract String toString(Object value);

	/**
	 * Convert the given string to an object of choice.
	 * 
	 * @param value
	 *            The string to convert, may be null
	 * @return The string value converted to an object of choice
	 */
	public abstract Object toObject(String value);
}
