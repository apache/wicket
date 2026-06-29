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
package org.apache.wicket.core.util.string;

import org.apache.wicket.request.Response;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.AttributeMap;

/**
 * Provide some helpers to write JSON-related tags to the response object.
 * 
 * @author Juergen Donnerstag
 */
public class JsonUtils
{
	/**
	 * Write the simple text to the response object surrounded by a script tag. <a
	 * href=" https://html.spec.whatwg.org/multipage/scripting.html#restrictions-for-contents-of-script-elements">Escapes
	 * <code>&lt;</code> to <code>\u005Cu003C</code></a>.
	 *
	 * @param response
	 * 		The HTTP: response
	 * @param text
	 * 		The text to added in between the script tags
	 * @param attributes
	 * 		Extra tag attributes. See constants prefixed with <code>ATTR_</code> in {@link JavaScriptUtils}
	 */
	public static void writeInlineScript(final Response response, final CharSequence text, AttributeMap attributes)
	{
		response.write("<script");
		response.write(attributes.toCharSequence());
		response.write(">");
		response.write(Strings.replaceAll(text, "<", "\\u003C"));
		response.write("</script>\n");
	}
}
