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
package org.apache.wicket.resource;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.util.string.Strings;

/**
 * Utilities for resources.
 * 
 * @author Jeremy Thomerson
 */
public class ResourceUtil
{

	private ResourceUtil()
	{
		// no-op
	}

	/**
	 * Helper that calls the proper IHeaderResponse.render*Reference method based on the input.
	 * 
	 * @param resp
	 *            the response to call render*Reference methods on
	 * @param ref
	 *            the reference to render
	 * @param css
	 *            true if this is a css reference
	 * @param string
	 *            the string argument to pass to those methods that accept it (js = id / css =
	 *            media)
	 */
	public static void renderTo(IHeaderResponse resp, ResourceReference ref, boolean css,
		String string)
	{
		if (css)
		{
			if (Strings.isEmpty(string))
			{
				resp.renderCSSReference(ref);
			}
			else
			{
				resp.renderCSSReference(ref, string);
			}
		}
		else
		{
			if (Strings.isEmpty(string))
			{
				resp.renderJavascriptReference(ref);
			}
			else
			{
				resp.renderJavascriptReference(ref, string);
			}
		}
	}
}
