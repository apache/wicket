/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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

import java.text.ParseException;
import java.util.Locale;

import javax.swing.text.MaskFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.WicketRuntimeException;

/**
 * A converter that takes a mask into account. It is specifically meant for
 * overrides on individual components, that provide their own converter by
 * returning it from {@link Component#getConverter(Object)}. It uses an
 * instance of {@link MaskFormatter} to delegate the masking and unmasking to.
 * <p>
 * The following characters can be specified (adopted from the MaskFormatter
 * documentation):
 * 
 * <table border=1 summary="Valid characters and their descriptions">
 * <tr>
 * <th>Character&nbsp;</th>
 * <th>
 * <p align="left">
 * Description
 * </p>
 * </th>
 * </tr>
 * <tr>
 * <td>#</td>
 * <td>Any valid number, uses <code>Character.isDigit</code>.</td>
 * </tr>
 * <tr>
 * <td>'</td>
 * <td>Escape character, used to escape any of the special formatting
 * characters.</td>
 * </tr>
 * <tr>
 * <td>U</td>
 * <td>Any character (<code>Character.isLetter</code>). All lowercase
 * letters are mapped to upper case.</td>
 * </tr>
 * <tr>
 * <td>L</td>
 * <td>Any character (<code>Character.isLetter</code>). All upper case
 * letters are mapped to lower case.</td>
 * </tr>
 * <tr>
 * <td>A</td>
 * <td>Any character or number (<code>Character.isLetter</code> or
 * <code>Character.isDigit</code>)</td>
 * </tr>
 * <tr>
 * <td>?</td>
 * <td>Any character (<code>Character.isLetter</code>).</td>
 * </tr>
 * <tr>
 * <td>*</td>
 * <td>Anything.</td>
 * </tr>
 * <tr>
 * <td>H</td>
 * <td>Any hex character (0-9, a-f or A-F).</td>
 * </tr>
 * </table>
 * 
 * <p>
 * Typically characters correspond to one char, but in certain languages this is
 * not the case. The mask is on a per character basis, and will thus adjust to
 * fit as many chars as are needed.
 * </p>
 * 
 * @see MaskFormatter
 * 
 * @author Eelco Hillenius
 */
public class MaskConverter implements IConverter
{
	private static final long serialVersionUID = 1L;

	/** Log. */
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(MaskConverter.class);

	/** Object that knows all about masks. */
	private final MaskFormatter maskFormatter;

	/**
	 * Construct.
	 * 
	 * @param mask
	 *            The mask to use for this converter instance
	 * @param type
	 *            The type to convert string values to. WARNING: adding anything
	 *            that implements charsequence here will probably not have the
	 *            desired effect as then only {@link #toString(Object)} will be
	 *            called. Consider wrapping your string value in a custom class
	 *            so that conversion will be triggered properly. That class
	 *            should have a public constructor with a single string
	 *            argument. That constructor will be used by
	 *            {@link MaskFormatter} to construct instances.
	 * @see MaskFormatter
	 */
	public MaskConverter(String mask, Class type)
	{
		try
		{
			maskFormatter = new MaskFormatter(mask);
			maskFormatter.setValueClass(type);
			maskFormatter.setAllowsInvalid(true);
			maskFormatter.setValueContainsLiteralCharacters(true);
		}
		catch (ParseException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Construct. WARNING: setting {@link MaskFormatter#setValueClass(Class)} to
	 * anything that implements charsequence, or not setting that class at all,
	 * which has the effect that String will be used will probably not have the
	 * desired effect as then only {@link #toString(Object)} will be called.
	 * Consider wrapping your string value in a custom class so that conversion
	 * will be triggered properly. That class should have a public constructor
	 * with a single string argument. That constructor will be used by
	 * {@link MaskFormatter} to construct instances.
	 * 
	 * @param maskFormatter
	 *            The mask formatter to use for masking and unmasking values
	 */
	public MaskConverter(MaskFormatter maskFormatter)
	{
		if (maskFormatter == null)
		{
			throw new IllegalArgumentException("argument maskFormatter may not be null");
		}

		this.maskFormatter = maskFormatter;
	}

	/**
	 * Converts the value to a string using
	 * {@link MaskFormatter#valueToString(Object)}.
	 * 
	 * @see wicket.util.convert.IConverter#convertToString(java.lang.Object,
	 *      Locale)
	 */
	public String convertToString(Object value, Locale locale)
	{
		try
		{
			return maskFormatter.valueToString(value);
		}
		catch (ParseException e)
		{
			throw new ConversionException(e);
		}
	}

	/**
	 * Converts a string to an object using
	 * {@link MaskFormatter#stringToValue(String)}.
	 * 
	 * @see wicket.util.convert.IConverter#convertToObject(java.lang.String,
	 *      Locale)
	 */
	public Object convertToObject(String value, Locale locale)
	{
		try
		{
			return maskFormatter.stringToValue(value);
		}
		catch (ParseException e)
		{
			throw new ConversionException(e);
		}
	}

}
