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

import org.apache.wicket.request.Response;
import org.apache.wicket.util.string.Strings;

/**
 * Base for text renderers that simply want to show a string
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 */
public abstract class AbstractAutoCompleteTextRenderer<T> extends AbstractAutoCompleteRenderer<T>
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void renderChoice(final T object, final Response response, final String criteria)
	{
		String textValue = getTextValue(object);
		textValue = Strings.escapeMarkup(textValue).toString();
		response.write(textValue);
	}
}
