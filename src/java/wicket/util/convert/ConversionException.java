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
{ // TODO finalize javadoc
	/** serialVersionUID */
	private static final long serialVersionUID = 3751845072374225603L;

    // these fields can be set by converters, but might be null!.
	
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
     * Get converter.
     * @return converter.
     */
    public final Converter getConverter()
    {
        return converter;
    }

    /**
     * Set converter.
     * @param converter converter.
     * @return This
     */
    public final ConversionException setConverter(Converter converter)
    {
        this.converter = converter;

        return this;
    }

    /**
     * Get targetType.
     * @return targetType.
     */
    public final Class getTargetType()
    {
        return targetType;
    }

    /**
     * Set targetType.
     * @param targetType targetType.
     * @return This
     */
    public final ConversionException setTargetType(Class targetType)
    {
        this.targetType = targetType;

        return this;
    }

    /**
     * Get triedValue.
     * @return triedValue.
     */
    public final Object getTriedValue()
    {
        return triedValue;
    }

    /**
     * Set triedValue.
     * @param triedValue triedValue.
     * @return This
     */
    public final ConversionException setTriedValue(Object triedValue)
    {
        this.triedValue = triedValue;

        return this;
    }

    /**
     * Get pattern.
     * @return pattern.
     */
    public final String getPattern()
    {
        return pattern;
    }

    /**
     * Set pattern.
     * @param pattern pattern.
     * @return This
     */
    public final ConversionException setPattern(String pattern)
    {
        this.pattern = pattern;

        return this;
    }

    /**
     * Get locale.
     * @return locale.
     */
    public final Locale getLocale()
    {
        return locale;
    }

    /**
     * Set locale.
     * @param locale locale.
     * @return This
     */
    public final ConversionException setLocale(Locale locale)
    {
        this.locale = locale;

        return this;
    }
}
