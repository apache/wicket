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

import java.awt.Component;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.text.MaskFormatter;

import org.apache.wicket.util.lang.Args;


/**
 * A converter that takes a mask into account. It is specifically meant for overrides on individual
 * components, that provide their own converter by returning it from
 * {@link Component#getConverter(Class)}. It uses an instance of {@link MaskFormatter} to delegate
 * the masking and unmasking to.
 * <p>
 * The following characters can be specified (adopted from the MaskFormatter documentation):
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
 * <td>Escape character, used to escape any of the special formatting characters.</td>
 * </tr>
 * <tr>
 * <td>U</td>
 * <td>Any character (<code>Character.isLetter</code>). All lowercase letters are mapped to upper
 * case.</td>
 * </tr>
 * <tr>
 * <td>L</td>
 * <td>Any character (<code>Character.isLetter</code>). All upper case letters are mapped to lower
 * case.</td>
 * </tr>
 * <tr>
 * <td>A</td>
 * <td>Any character or number (<code>Character.isLetter</code> or <code>Character.isDigit</code>)</td>
 * </tr>
 * <tr>
 * <td>?</td>
 * <td>Any character (<code>Character.isLetter</code>).</td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>Anything.</td>
 * </tr>
 * <tr>
 * <td>H</td>
 * <td>Any hex character (0-9, a-f or A-F).</td>
 * </tr>
 * </table>
 * 
 * <p>
 * Typically characters correspond to one char, but in certain languages this is not the case. The
 * mask is on a per character basis, and will thus adjust to fit as many chars as are needed.
 * </p>
 * 
 * @see MaskFormatter
 * 
 * @author Eelco Hillenius
 * @param <C>
 */
public class MaskConverter<C> implements IConverter<C>
{
	private static final long serialVersionUID = 1L;

	/** Object that knows all about masks. */
	private final MaskFormatter maskFormatter;

	/**
	 * Construct.
	 * 
	 * @param maskFormatter
	 *            The mask formatter to use for masking and unmasking values
	 */
	public MaskConverter(final MaskFormatter maskFormatter)
	{
		Args.notNull(maskFormatter, "maskFormatter");

		this.maskFormatter = maskFormatter;
	}

	/**
	 * Construct; converts to Strings.
	 * 
	 * @param mask
	 *            The mask to use for this converter instance
	 * @see MaskFormatter
	 */
	public MaskConverter(final String mask)
	{
		this(mask, String.class);
	}

	/**
	 * Construct.
	 * 
	 * @param mask
	 *            The mask to use for this converter instance
	 * @param type
	 *            The type to convert string values to.
	 * @see MaskFormatter
	 */
	public MaskConverter(final String mask, final Class<?> type)
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
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts a string to an object using {@link MaskFormatter#stringToValue(String)}.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public C convertToObject(final String value, final Locale locale)
	{
		try
		{
			return (C)maskFormatter.stringToValue(value);
		}
		catch (ParseException e)
		{
			throw new ConversionException(e);
		}
	}

	/**
	 * Converts the value to a string using {@link MaskFormatter#valueToString(Object)}.
	 */
	@Override
	public String convertToString(final C value, final Locale locale)
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
}
