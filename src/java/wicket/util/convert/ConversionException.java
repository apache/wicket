/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.util.convert;

import java.text.Format;
import java.util.Locale;

/**
 * Thrown for conversion exceptions.
 * 
 * @author Eelco Hillenius
 */
public final class ConversionException extends RuntimeException
{
	/** serialVersionUID. */
	private static final long serialVersionUID = 3751845072374225603L;

	/** The converter that was used. */
	private IConverter converter;

	/** Pattern that was used for conversion. */
	private Format format;

	/** The value that was tried to convert. */
	private Object sourceValue;

	/** Target type for the failed conversion. */
	private Class targetType;

	/** The type converter that was used */
	private ITypeConverter typeConverter;

	/**
	 * Construct exception with message.
	 * 
	 * @param message
	 *            message
	 */
	public ConversionException(String message)
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
	public ConversionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Construct exception with cause.
	 * 
	 * @param cause
	 *            cause
	 */
	public ConversionException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Gets the used converter.
	 * 
	 * @return the used converter.
	 */
	public final IConverter getConverter()
	{
		return converter;
	}
    
    /**
     * @return Locale
     */
    public final Locale getLocale()
    {
    	if (converter != null)
        {
    		return converter.getLocale();
        }
        if (typeConverter != null)
        {
        	return typeConverter.getLocale();
        }
        return null;
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
	public final Class getTargetType()
	{
		return targetType;
	}

	/**
	 * @return Returns the typeConverter.
	 */
	public ITypeConverter getTypeConverter()
	{
		return typeConverter;
	}

	/**
	 * Sets the used converter.
	 * 
	 * @param converter
	 *            the converter.
	 * @return This
	 */
	public final ConversionException setConverter(IConverter converter)
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
	public final ConversionException setFormat(Format format)
	{
		this.format = format;
		return this;
	}

	/**
	 * Sets the tried value.
	 * 
	 * @param sourceValue
	 *            the tried value.
	 * @return This
	 */
	public final ConversionException setSourceValue(Object sourceValue)
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
	public final ConversionException setTargetType(Class targetType)
	{
		this.targetType = targetType;
		return this;
	}

	/**
	 * @param typeConverter
	 *            The typeConverter to set.
	 * @return This
	 */
	public ConversionException setTypeConverter(ITypeConverter typeConverter)
	{
		this.typeConverter = typeConverter;
		return this;
	}
}