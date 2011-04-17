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
package org.apache.wicket.util.convert;

import java.text.Format;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;


/**
 * Thrown for conversion exceptions.
 * 
 * @author Eelco Hillenius
 */
public class ConversionException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/** The converter that was used. */
	private IConverter<?> converter;

	/** Pattern that was used for conversion. */
	private Format format;

	/** Locale that was used for conversion. */
	private Locale locale;

	/** The value that was tried to convert. */
	private Object sourceValue;

	/** Target type for the failed conversion. */
	private Class<?> targetType;

	/** Resource key for the message that should be displayed */
	private String resourceKey;

	/** Variable map to use in variable substitution */
	private Map<String, Object> vars;

	/**
	 * Construct exception with message.
	 * 
	 * @param message
	 *            message
	 */
	public ConversionException(final String message)
	{
		super(message);
	}

	/**
	 * Construct exception with message and cause.
	 * 
	 * @param message
	 *            message
	 * @param cause
	 *            cause
	 */
	public ConversionException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Construct exception with cause.
	 * 
	 * @param cause
	 *            cause
	 */
	public ConversionException(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Gets the used converter.
	 * 
	 * @return the used converter.
	 */
	public final IConverter<?> getConverter()
	{
		return converter;
	}

	/**
	 * Get the used format.
	 * 
	 * @return the used format
	 */
	public final Format getFormat()
	{
		return format;
	}

	/**
	 * Get the used locale.
	 * 
	 * @return the used locale
	 */
	public final Locale getLocale()
	{
		return locale;
	}

	/**
	 * Gets the tried value.
	 * 
	 * @return the tried value.
	 */
	public final Object getSourceValue()
	{
		return sourceValue;
	}

	/**
	 * Gets the target property type.
	 * 
	 * @return the target property type.
	 */
	public final Class<?> getTargetType()
	{
		return targetType;
	}

	/**
	 * Sets the used converter.
	 * 
	 * @param converter
	 *            the converter.
	 * @return This
	 */
	public final ConversionException setConverter(final IConverter<?> converter)
	{
		this.converter = converter;
		return this;
	}

	/**
	 * Sets the used format.
	 * 
	 * @param format
	 *            the used format.
	 * @return This
	 */
	public final ConversionException setFormat(final Format format)
	{
		this.format = format;
		return this;
	}

	/**
	 * Sets the used locale.
	 * 
	 * @param locale
	 *            the used locale.
	 * @return This
	 */
	public final ConversionException setLocale(final Locale locale)
	{
		this.locale = locale;
		return this;
	}

	/**
	 * Sets the tried value.
	 * 
	 * @param sourceValue
	 *            the tried value.
	 * @return This
	 */
	public final ConversionException setSourceValue(final Object sourceValue)
	{
		this.sourceValue = sourceValue;
		return this;
	}

	/**
	 * Sets the target property type.
	 * 
	 * @param targetType
	 *            sets the target property type
	 * @return This
	 */
	public final ConversionException setTargetType(final Class<?> targetType)
	{
		this.targetType = targetType;
		return this;
	}


	/**
	 * @return The resource key for the message that should be displayed
	 */
	public String getResourceKey()
	{
		return resourceKey;
	}


	/**
	 * Set the resource key for the message that should be displayed.
	 * 
	 * @param resourceKey
	 *            sets the resource key
	 * @return This
	 */
	public ConversionException setResourceKey(final String resourceKey)
	{
		this.resourceKey = resourceKey;
		return this;
	}

	/**
	 * Sets a variable that will be used in substitution
	 * 
	 * @param name
	 *            variable name
	 * @param value
	 *            variable value
	 * @return this for chaining
	 */
	public ConversionException setVariable(final String name, final Object value)
	{
		Args.notEmpty(name, "name");
		Args.notNull(value, "value");

		if (vars == null)
		{
			vars = Generics.newHashMap(2);
		}

		vars.put(name, value);
		return this;
	}

	/**
	 * Returns the map of variables for this exception.
	 * 
	 * @return map of variables for this exception (or null if no variables were defined)
	 */
	public Map<String, Object> getVariables()
	{
		return vars;
	}
}