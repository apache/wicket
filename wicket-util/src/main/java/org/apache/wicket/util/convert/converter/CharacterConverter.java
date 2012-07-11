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
package org.apache.wicket.util.convert.converter;

import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;


/**
 * Converts from Object to Character.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class CharacterConverter extends AbstractConverter<Character>
{
	private static final long serialVersionUID = 1L;

	/**
	 * The singleton instance for a character converter
	 */
	public static final IConverter<Character> INSTANCE = new CharacterConverter();

	/**
	 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,Locale)
	 */
	@Override
	public Character convertToObject(final String value, final Locale locale)
	{
		int length = value.length();
		if (length == 0)
		{
			return null;
		}
		else if (length == 1)
		{
			return value.charAt(0);
		}
		throw newConversionException("Cannot convert '" + value + "' to Character", value, locale);
	}

	/**
	 * @see org.apache.wicket.util.convert.converter.AbstractConverter#getTargetType()
	 */
	@Override
	protected Class<Character> getTargetType()
	{
		return Character.class;
	}
}