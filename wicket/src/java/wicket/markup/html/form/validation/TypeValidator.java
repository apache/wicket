/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import java.util.Locale;
import java.util.Map;

import wicket.Session;
import wicket.markup.html.form.FormComponent;
import wicket.util.convert.ConversionException;
import wicket.util.convert.IConverter;
import wicket.util.string.Strings;

/**
 * Validates input by trying it to convert to the given type using the
 * {@link wicket.util.convert.IConverter}instance of the component doing the
 * validation.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class TypeValidator extends AbstractValidator
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
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[TypeValidator type = " + type + ", locale = " + getLocale() + "]";
	}

	/**
	 * Validates input by trying it to convert to the given type using the
	 * {@link wicket.util.convert.IConverter}instance of the component doing
	 * the validation.
	 * 
	 * @param component
	 *            The component that wants to validate its input
	 * @see wicket.markup.html.form.validation.IValidator#validate(wicket.markup.html.form.FormComponent)
	 */
	public final void validate(FormComponent component)
	{
		// Get component value
		final String value = component.getRequestString();

		// If value is non-empty
		if (!Strings.isEmpty(value))
		{
			// Check value by attempting to convert it using the given locale
			final IConverter converter = component.getConverter();
			converter.setLocale(getLocale());
			try
			{
				converter.convert(value, type);
			}
			catch (ConversionException e)
			{
				conversionError(value, component, e);
			}
		}
	}

	/**
	 * Gets the error message.
	 * 
	 * @param input
	 *            The input
	 * @param component
	 *            the component
	 * @param e
	 *            the conversion exception
	 */
	protected void conversionError(String input, FormComponent component, ConversionException e)
	{
		error(getResourceKey(component), messageModel(component, input, e), input, component);
	}

	/**
	 * Gets the message context.
	 * 
	 * @param input
	 *            The input
	 * @param component
	 *            the component
	 * @param e
	 *            the conversion exception
	 * @return a map with variables for interpolation
	 */
	protected Map messageModel(FormComponent component, String input, ConversionException e)
	{
		Map model = super.messageModel(component, input);
		model.put("type", type);
		Locale locale = e.getLocale();
		if (locale != null)
		{
			model.put("locale", locale);
		}
		model.put("exception", e.getMessage());
		model.put("pattern", e.getPattern());
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