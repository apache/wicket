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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

/**
 * The default template engine, shipped with the table's script.
 * <p>
 * It knows two placeholders and nothing else: {@code {{a.b}}} writes the value at the dotted
 * path, HTML-escaped, and {@code {{{a.b}}}} writes it unescaped. A missing value writes nothing.
 * It does not evaluate code, so it works under a Content Security Policy without
 * {@code unsafe-eval}.
 *
 * @since 11.0.0
 */
public class BuiltInTemplateEngine implements ITemplateEngine
{
	private static final long serialVersionUID = 1L;

	/** The shared instance. */
	public static final BuiltInTemplateEngine INSTANCE = new BuiltInTemplateEngine();

	@Override
	public String getName()
	{
		return "builtin";
	}
}
