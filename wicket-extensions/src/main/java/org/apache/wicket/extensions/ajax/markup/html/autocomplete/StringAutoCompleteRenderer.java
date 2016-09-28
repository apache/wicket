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
package org.apache.wicket.extensions.ajax.markup.html.autocomplete;

/**
 * An renderer that calls object.toString() to get the text value. Great for quickly generating a
 * list of assists.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public final class StringAutoCompleteRenderer extends AbstractAutoCompleteTextRenderer<Object>
{
	private static final long serialVersionUID = 1L;

	/**
	 * A singleton instance
	 */
	@SuppressWarnings("rawtypes")
	public static final IAutoCompleteRenderer INSTANCE = new StringAutoCompleteRenderer();

	/**
	 * @param <T>
	 * @return the single instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> IAutoCompleteRenderer<T> instance()
	{
		return INSTANCE;
	}

	@Override
	protected String getTextValue(final Object object)
	{
		return object.toString();
	}
}
