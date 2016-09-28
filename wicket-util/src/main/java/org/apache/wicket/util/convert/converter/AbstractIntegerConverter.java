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

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Base class for all converters of integer numbers.
 * 
 * @author Jonathan Locke
 * @param <I>
 */
public abstract class AbstractIntegerConverter<I extends Number> extends AbstractNumberConverter<I>
{
	private static final long serialVersionUID = 1L;

	@Override
	protected NumberFormat newNumberFormat(Locale locale)
	{
		NumberFormat numberFormat  = NumberFormat.getIntegerInstance(locale);
		numberFormat.setParseIntegerOnly(true);
		numberFormat.setGroupingUsed(false);
		return numberFormat;
	}
}
