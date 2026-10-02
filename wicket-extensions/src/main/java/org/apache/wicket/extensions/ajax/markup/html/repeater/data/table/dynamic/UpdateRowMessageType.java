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

import org.apache.wicket.util.lang.Args;

/**
 * Messages of this type repaint a single row of the table from the data they carry, leaving the
 * other rows alone. The row is found by its key; a message for a row the browser does not show,
 * for example one on another page, is ignored. Every {@link DynamicDataTable} registers it;
 * {@link DynamicDataTable#newUpdateRowMessage(Object)} builds such a message, and
 * {@link #newMessage(String, String, Object)} builds one without the table, for example on a
 * background thread.
 *
 * @since 11.0.0
 */
public final class UpdateRowMessageType implements IWebSocketMessageType
{
	private static final long serialVersionUID = 1L;

	/** The shared instance. */
	public static final UpdateRowMessageType INSTANCE = new UpdateRowMessageType();

	/** The id of the type. */
	public static final String TYPE_ID = "updateRow";

	private UpdateRowMessageType()
	{
	}

	/**
	 * Builds a message repainting a row, sent as {@code tableId}, {@code typeId}, {@code key} and
	 * {@code data}. Every property the serializer produces for {@code data} reaches the browser.
	 *
	 * @param tableId
	 *            the markup id of the table
	 * @param key
	 *            the key of the row as the browser shows it, see
	 *            {@link DynamicDataTable#getRowKey(Object)}
	 * @param data
	 *            the row, serialized when the message is sent
	 * @return the message
	 */
	public static IWebSocketLightWeightMessage newMessage(String tableId, String key, Object data)
	{
		return new DynamicDataTable.UpdateRowMessage(Args.notEmpty(tableId, "tableId"),
			Args.notNull(key, "key"), data);
	}

	@Override
	public String getTypeId()
	{
		return TYPE_ID;
	}

	@Override
	public String getTypeFunction()
	{
		return "function (message) { " +
			"Wicket.DynamicDataTable.instances[message.tableId].renderRow(message.key, " +
			"message.data); }";
	}
}
