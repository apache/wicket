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
package org.apache.wicket.util.time;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 
 * Utility class for {@link Instant}
 *
 */
public class Instants
{

	public static final DateTimeFormatter RFC7231DateFormatter = DateTimeFormatter
		.ofPattern("EEE, dd MMM yyyy HH:mm:ss O", Locale.ENGLISH).withZone(ZoneId.of("UTC"));

	public static final DateTimeFormatter localDateFormatter = DateTimeFormatter
		.ofPattern("yyyy.MM.dd", Locale.ENGLISH);

	/**
	 * Formats a given {@link Instant} as required by RFC7231 for dates.
	 * 
	 * @param instant
	 * @return the instant properly formatted
	 */
	public static String toRFC7231Format(final Instant instant)
	{
		return RFC7231DateFormatter.format(instant);
	}
}
