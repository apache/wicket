/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.form.validation;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import wicket.Session;
import wicket.util.convert.ConversionException;
import wicket.util.convert.IConverter;
import wicket.util.string.Strings;

/**
 * Validates input by trying it to convert to the given type using the
 * {@link wicket.util.convert.IConverter}instance of the component doing the
 * validation.
 * <p>
 * This component adds ${type}, ${exception}, ${locale} and ${format} to the
 * model for OGNL error message interpolation. Format is only valid if the type
 * conversion involves a date.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class TypeValidator extends StringValidator
{
	/** The locale to use */
	private Locale locale = null;

	/** The type to use for checking. */
	private Class type;

	/**
	 * Constructor. The current session's locale will be used for conversion.
	 * 
	 * @param type
	 *            The type to use for checking
	 */
	public TypeValidator(final Class type)
	{
		this.type = type;
	}

	/**
	 * Construct. If not-null, the given locale will be used for conversion.
	 * Otherwise the session's locale will be used for conversion.
	 * 
	 * @param type
	 *            The type to use for checking
	 * @param locale
	 *            The locale to use
	 */
	public TypeValidator(final Class type, final Locale locale)
	{
		this.type = type;
		this.locale = locale;
	}

	/**
	 * Gets the type to use for checking.
	 * 
	 * @return the type to use for checking
	 */
	public final Class getType()
	{
		return type;
	}

	/**
	 * Validates input by trying it to convert to the given type using the
	 * {@link wicket.util.convert.IConverter}instance of the component doing
	 * the validation.
	 * 
	 * @see wicket.markup.html.form.validation.StringValidator#onValidate(java.lang.String)
	 */
	public void onValidate(String value)
	{
		// If value is non-empty
		if (!Strings.isEmpty(value))
		{
			// Check value by attempting to convert it
			final IConverter converter = getFormComponent().getConverter();
			try
			{
				converter.convert(value, type);
			}
			catch (Exception e)
			{
				if(e instanceof ConversionException)
				{
					error(messageModel((ConversionException)e));
				}
				else
				{
					error(messageModel(new ConversionException(e)));
				}
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[TypeValidator type = " + type + ", locale = " + getLocale() + "]";
	}

	/**
	 * Gets the message context.
	 * 
	 * @param e
	 *            the conversion exception
	 * @return a map with variables for interpolation
	 */
	protected Map messageModel(final ConversionException e)
	{
		final Map model = super.messageModel();
		model.put("type", type);
		final Locale locale = e.getLocale();
		if (locale != null)
		{
			model.put("locale", locale);
		}
		model.put("exception", e);
		Format format = e.getFormat();
		if (format instanceof SimpleDateFormat)
		{
			model.put("format", ((SimpleDateFormat)format).toLocalizedPattern());
		}
		return model;
	}

	/**
	 * Gets the locale to use. if null and useLocaled == true, the session's
	 * locale will be used..
	 * 
	 * @return the locale to use
	 */
	private final Locale getLocale()
	{
		if (locale == null)
		{
			return Session.get().getLocale();
		}
		return locale;
	}
}