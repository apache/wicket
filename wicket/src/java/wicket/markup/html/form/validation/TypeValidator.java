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
package wicket.markup.html.form.validation;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.ApplicationSettings;
import wicket.RequestCycle;
import wicket.markup.html.form.FormComponent;
import wicket.util.convert.ConversionException;
import wicket.util.convert.ConversionUtils;
import wicket.util.convert.ConverterRegistry;

/**
 * Validates input by trying it to convert to the given type using the
 * {@link wicket.util.convert.ConverterRegistry} instance of that can be
 * found in the application settings.
 *
 * @author Eelco Hillenius
 */
public class TypeValidator extends AbstractValidator
{
	/** Log. */
	private static Log log = LogFactory.getLog(TypeValidator.class);

	/** the type to use for checking. */
	private Class type;

	/**
	 * the locale to use. if null and useLocaled == true, the session's locale
	 * will be used.
	 */
	private Locale locale = null;

	/** whether to use either the set locale or the session's locale. */
	private boolean useLocalized = true;

	/**
	 * Construct. The current session's locale will be used for conversion.
	 * @param type the type to use for checking
	 */
	public TypeValidator(Class type)
	{
		this(type, null);
	}

	/**
	 * Construct. If not-null, the given locale will be used for conversion. Otherwise
	 * the session's locale will be used for conversion.
	 * @param type the type to use for checking
	 * @param locale the locale to use
	 */
	public TypeValidator(Class type, Locale locale)
	{
		this.type = type;
		this.locale = locale;
	}

	/**
	 * Construct. If useLocalized is true, the current session's locale will
	 * be used for conversion. If useLocalized is false, no localization will
	 * be used for checking.
	 * @param type the type to use for checking
	 * @param useLocalized whether localization (using the current session's locale)
	 * should be used
	 */
	public TypeValidator(Class type, boolean useLocalized)
	{
		this(type, null);
		this.useLocalized = useLocalized;
	}

	/**
	 * @see wicket.markup.html.form.validation.IValidator#validate(java.io.Serializable, wicket.markup.html.form.FormComponent)
	 */
	public ValidationErrorMessage validate(Serializable input, FormComponent component)
	{
		ConversionUtils conversionUtils = getConversionUtils();
		Locale localeForValidation = getLocaleForValidation();
		try
		{
			conversionUtils.convert(input, type, localeForValidation);
			return ValidationErrorMessage.NO_MESSAGE;
		}
		catch (ConversionException e)
		{
			return getError(input, component, e);
		}
	}

	/**
	 * Gets the error message.
	 * @param input the input
	 * @param component the component
	 * @param e the conversion exception
	 * @return the validation error message
	 */
	protected ValidationErrorMessage getError(Serializable input,
			FormComponent component, ConversionException e)
	{
		Map ctx = getMessageContext(input, component, e);
		return errorMessage(getResourceKey(component), ctx, input, component);
	}

	/**
	 * Gets the message context.
	 * @param input the input
	 * @param component the component
	 * @param e the conversion exception
	 * @return a map with variables for interpolation
	 */
	protected Map getMessageContext(Serializable input,
			FormComponent component, ConversionException e)
	{
		Map ctx = super.getMessageContextVariables(input, component);
		ctx.put("type", type);
		Locale loc = e.getLocale();
		if(loc != null) ctx.put("locale", loc);
		ctx.put("exception", e.getMessage());
		ctx.put("pattern", e.getPattern());
		return ctx;
	}
	/**
	 * Gets the conversion utilities.
	 * @return the conversion utilities
	 */
	protected ConversionUtils getConversionUtils()
	{
		RequestCycle requestCycle = RequestCycle.get();
		if(requestCycle != null)
		{
			ApplicationSettings settings = requestCycle.getApplication().getSettings();
			ConverterRegistry converterRegistry = settings.getConverterRegistry();
			return converterRegistry.getConversionUtils();
		}
		else // we must be in a test case; just give a dummy
		{
			log.error("no current request cycle found; using a dummy converter registry");
			return new ConversionUtils(new ConverterRegistry());
		}
	}

	/**
	 * Gets the locale that should be used for the current validation.
	 * @return the locale that should be used for the current validation
	 */
	private Locale getLocaleForValidation()
	{
		Locale localeForValidation = null;
		if(isUseLocalized())
		{
			localeForValidation = getLocale();
			if(localeForValidation == null)
			{
				localeForValidation = RequestCycle.get().getSession().getLocale();
			}
		}
		return localeForValidation;
	}

	/**
	 * Gets the locale to use. if null and useLocaled == true, the session's locale
	 * will be used..
	 * @return the locale to use
	 */
	protected Locale getLocale()
	{
		return locale;
	}

	/**
	 * Gets the type to use for checking.
	 * @return the type to use for checking
	 */
	protected Class getType()
	{
		return type;
	}

	/**
	 * Gets whether to use either the set locale or the session's locale.
	 * @return whether to use either the set locale or the session's locale.
	 */
	protected boolean isUseLocalized()
	{
		return useLocalized;
	}
}
