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

import org.apache.wicket.util.io.IClusterable;

/**
 * Turns the rows of a {@link DynamicDataTable} into JSON.
 * <p>
 * Whatever a row serializes to is sent to the browser, where every column template can read it.
 * A property that must not reach the browser has to be left out here.
 *
 * @since 11.0.0
 */
@FunctionalInterface
public interface IJsonSerializer extends IClusterable
{
	/**
	 * @param value
	 *            a {@link java.util.Map} or {@link java.util.List} containing the rows, or a row
	 * @return the JSON
	 */
	String toJson(Object value);
}
