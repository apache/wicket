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

import java.util.Locale;

/**
 * Thrown for conversion exceptions.
 */
public final class ConversionException extends RuntimeException
{
	/** serialVersionUID. */
	private static final long serialVersionUID = 3751845072374225603L;
	
	/**
     * Target type for the failed conversion.
     */
    private Class targetType;

    /**
     * The converter that was used.
     */
    private Converter converter;

    /**
     * The value that was tried to convert.
     */
    private Object triedValue;

    /**
     * Pattern that was used for conversion.
     */
    private String pattern;

    /**
     * Locale that was used for the conversion.
     */
    private Locale locale;

    /**
     * Construct exception with message.
     * @param message message
     */
    public ConversionException(String message)
    {
        super(message);
    }

    /**
     * Construct exception with message and cause.
     * @param message message
     * @param cause cause
     */
    public ConversionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Construct exception with cause.
     * @param cause cause
     */
    public ConversionException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Gets the used converter.
     * @return the used converter.
     */
    public final Converter getConverter()
    {
        return converter;
    }

    /**
     * Sets the used converter.
     * @param converter the converter.
     * @return This
     */
    public final ConversionException setConverter(Converter converter)
    {
        this.converter = converter;
        return this;
    }

    /**
     * Gets the target property type.
     * @return the target property type.
     */
    public final Class getTargetType()
    {
        return targetType;
    }

    /**
     * Sets the target property type.
     * @param targetType sets the target property type
     * @return This
     */
    public final ConversionException setTargetType(Class targetType)
    {
        this.targetType = targetType;
        return this;
    }

    /**
     * Gets the tried value.
     * @return the tried value.
     */
    public final Object getTriedValue()
    {
        return triedValue;
    }

    /**
     * Sets the tried value.
     * @param triedValue the tried value.
     * @return This
     */
    public final ConversionException setTriedValue(Object triedValue)
    {
        this.triedValue = triedValue;
        return this;
    }

    /**
     * Get the used pattern.
     * @return the used pattern
     */
    public final String getPattern()
    {
        return pattern;
    }

    /**
     * Sets the used pattern.
     * @param pattern the used pattern.
     * @return This
     */
    public final ConversionException setPattern(String pattern)
    {
        this.pattern = pattern;
        return this;
    }

    /**
     * Gets the used locale.
     * @return the used locale.
     */
    public final Locale getLocale()
    {
        return locale;
    }

    /**
     * Sets the used locale.
     * @param locale the used locale.
     * @return This
     */
    public final ConversionException setLocale(Locale locale)
    {
        this.locale = locale;
        return this;
    }
}