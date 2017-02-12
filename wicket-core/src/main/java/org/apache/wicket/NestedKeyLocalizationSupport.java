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
package org.apache.wicket;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Finds nested keys and replaces them with the default localizer it uses the pattern {{key}}. Also
 * nested keys can be found.
 * 
 * @author Tobias Soloschenko
 *
 */
public class NestedKeyLocalizationSupport implements ILocalizationSupport
{

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^ ]*?)\\}\\}");

	@Override
	public String resolveString(Localizer localizer, String key, Component component, IModel<?> model,
		Locale locale, String style, IModel<String> defaultValue, String previousTranslatedString)
			throws MissingResourceException
	{
		StringBuffer output = new StringBuffer();
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(previousTranslatedString);
		// Search for other nested keys to replace
		while (matcher.find())
		{
			String replacedPlaceHolder = localizer.resolveStringWithLocalizationSupport(matcher.group(1),
				component, model, locale, style, (Model<String>)null);
			matcher.appendReplacement(output, replacedPlaceHolder);
		}
		matcher.appendTail(output);
		return output.toString();
	}

}
