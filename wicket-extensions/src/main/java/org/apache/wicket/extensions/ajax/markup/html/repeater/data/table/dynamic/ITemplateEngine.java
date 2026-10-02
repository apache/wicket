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

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.util.io.IClusterable;

/**
 * The client-side engine a {@link DynamicDataTable} evaluates its column templates with.
 * <p>
 * An engine is registered in the browser under {@link #getName()} with
 * {@code Wicket.DynamicDataTable.engines[name] = {compile: function(template) {...}}},
 * where {@code compile} returns a {@code function(data, meta)} from a row's data to the cell's
 * markup. {@code meta} holds the row variables, {@code key} and {@code number}, which templates
 * refer to as {@code {{@key}}} and {@code {{@number}}}. The escaping of the values a template
 * refers to is the engine's responsibility.
 *
 * @since 11.0.0
 */
public interface ITemplateEngine extends IClusterable
{
	/**
	 * @return the name the engine is registered under in the browser
	 */
	String getName();

	/**
	 * Contributes whatever the engine needs in the browser, typically the script registering it.
	 * Called after the table's own script has been rendered.
	 *
	 * @param response
	 *            the header response
	 */
	default void renderHead(IHeaderResponse response)
	{
	}
}
