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
package org.apache.wicket.extensions.markup.html.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.string.Strings;


/**
 * This base implementation iterates over all provided <code>ILinkRenderStrategy</code>
 * implementations and applies them to the input text.
 * 
 * @author Gerolf Seitz
 */
public class LinkParser implements ILinkParser
{
	private final Map<String, ILinkRenderStrategy> renderStrategies = new HashMap<String, ILinkRenderStrategy>();

	/**
	 * Adds a render strategy to the parser.
	 * 
	 * @param pattern
	 *            the pattern to which the provided <code>renderStrategy</code> should be applied.
	 * @param renderStrategy
	 *            the <code>ILinkRenderStrategy</code> which is applied to the text found by the
	 *            provided <code>pattern</code>.
	 * @return this <code>ILinkParser</code>.
	 */
	public ILinkParser addLinkRenderStrategy(final String pattern,
		final ILinkRenderStrategy renderStrategy)
	{
		renderStrategies.put(pattern, renderStrategy);
		return this;
	}

	/**
	 * @see ILinkParser#parse(String)
	 */
	@Override
	public String parse(final String text)
	{
		if (Strings.isEmpty(text))
		{
			return text;
		}

		String work = text;

		// don't try to parse markup. just plain text. WICKET-4099
		if (work.indexOf('<') == -1)
		{
			for (String pattern : renderStrategies.keySet())
			{
				ILinkRenderStrategy strategy = renderStrategies.get(pattern);

				Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(work);
				StringBuffer buffer = new StringBuffer();
				while (matcher.find())
				{
					String str = matcher.group();
					matcher.appendReplacement(buffer, strategy.buildLink(str));
				}
				matcher.appendTail(buffer);
				work = buffer.toString();
			}
		}
		return work;
	}
}
