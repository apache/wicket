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
 * Messages of this type make the table fetch the rows of its current page again, and have the
 * navigation re-rendered if the number of rows or the current page changed. Every
 * {@link DynamicDataTable} registers it; {@link DynamicDataTable#newRefreshMessage()} builds such a
 * message.
 *
 * @since 11.0.0
 */
public final class RefreshMessageType implements IWebSocketMessageType
{
	private static final long serialVersionUID = 1L;

	/** The shared instance. */
	public static final RefreshMessageType INSTANCE = new RefreshMessageType();

	/** The id of the type. */
	public static final String TYPE_ID = "refresh";

	private RefreshMessageType()
	{
	}

	@Override
	public String getTypeId()
	{
		return TYPE_ID;
	}

	@Override
	public String getTypeFunction()
	{
		return "function (message) { Wicket.DynamicDataTable.instances[message.tableId].load(); }";
	}
}
