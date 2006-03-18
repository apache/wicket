/*
 * $Id: org.eclipse.jdt.ui.prefs,v 1.5 2005/11/26 10:32:55 eelco12 Exp $
 * $Revision: 1.5 $ $Date: 2005/11/26 10:32:55 $
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

/**
 * Adapter class to simplify implementing custom {@link IConverter}s. If the
 * requested type to convert to is a string type (or more precice, assignable
 * from {@link CharSequence}), this converter will delegate type conversion to
 * {@link #toString(Object)}. Otherwise, it will delegate type conversion to
 * {@link #toObject(String)} using the object's {@link Object#toString()} value
 * to convert it to a string first (or passing in null in case the value is
 * null).
 * 
 * @author Eelco Hillenius
 */
public abstract class SimpleConverterAdapter extends LocalizableAdapter implements IConverter
{
	private static final long serialVersionUID = 1L;

	/**
	 * If class c is a string type (or more precice, assignable from
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
		if (CharSequence.class.isAssignableFrom(c))
		{
			return toString(value);
		}
		else
		{
			return toObject((value != null) ? value.toString() : (String)value);
		}
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
