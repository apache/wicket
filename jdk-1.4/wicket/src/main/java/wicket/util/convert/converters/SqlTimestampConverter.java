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
package wicket.util.convert.converters;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

/**
 * Converts to {@link Timestamp}.
 * 
 * @author eelcohillenius
 */
public class SqlTimestampConverter extends DateConverter
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see wicket.util.convert.converters.DateConverter#convertToObject(java.lang.String,
	 *      java.util.Locale)
	 */
	public Object convertToObject(String value, Locale locale)
	{
		return new Timestamp(((Date)super.convertToObject(value, locale)).getTime());
	}

	/**
	 * @see wicket.util.convert.converters.DateConverter#getTargetType()
	 */
	protected Class getTargetType()
	{
		return Timestamp.class;
	}
}
