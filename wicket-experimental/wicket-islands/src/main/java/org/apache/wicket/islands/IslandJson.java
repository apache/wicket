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
package org.apache.wicket.islands;

/**
 * JSON helpers.
 */
final class IslandJson
{
	private IslandJson()
	{
	}

	/**
	 * Escapes JSON for the content of a <code>&lt;script&gt;</code> element. The characters
	 * replaced can only occur inside JSON strings, where their Unicode escapes mean the same.
	 * This makes <code>&lt;/script&gt;</code> and <code>&lt;!--</code> impossible in the output.
	 *
	 * @param json
	 *            the JSON text
	 * @return the escaped JSON text
	 */
	static String escapeForScript(CharSequence json)
	{
		StringBuilder escaped = new StringBuilder(json.length() + 16);
		for (int i = 0; i < json.length(); i++)
		{
			char c = json.charAt(i);
			switch (c)
			{
				case '<' -> escaped.append("\\u003c");
				case '>' -> escaped.append("\\u003e");
				case '&' -> escaped.append("\\u0026");
				case ' ' -> escaped.append("\\u2028");
				case ' ' -> escaped.append("\\u2029");
				default -> escaped.append(c);
			}
		}
		return escaped.toString();
	}
}
