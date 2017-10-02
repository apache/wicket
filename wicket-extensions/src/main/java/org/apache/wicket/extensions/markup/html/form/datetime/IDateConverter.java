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
package org.apache.wicket.extensions.markup.html.form.datetime;

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;


/**
 * Base class for javax.time based date converters. It contains the logic to parse and format,
 * optionally taking the time zone difference between clients and the server into account.
 * <p>
 * Converters of this class are best suited for per-component use.
 * </p>
 * 
 * @author eelcohillenius
 */
public interface IDateConverter<T extends Temporal> extends IConverter<T>
{

	T convertToObject(String value, DateTimeFormatter format, Locale locale);

	/**
	 * @param locale
	 *            The locale used to convert the value
	 * @return Gets the pattern that is used for printing and parsing
	 */
	String getPattern(Locale locale);

	/**
	 * @param locale
	 *            The locale used to convert the value
	 * 
	 * @return formatter The formatter for the current conversion
	 */
	DateTimeFormatter getFormat(Locale locale);
}
