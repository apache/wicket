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
package org.apache.wicket.islands.serializer;

/**
 * Converts the values exchanged between an island and its client-side peer to and from JSON:
 * the props written into the page, the payloads of calls and their results.
 * <p>
 * The JSON a serializer reads comes from the browser and is untrusted. An implementation must
 * only ever create instances of the type it is asked for and of the types that type declares. It
 * must never honour type information from the input itself (polymorphic type handling), because
 * that lets a client choose which classes are instantiated on the server.
 *
 * @since 12.0.0
 */
public interface IIslandSerializer
{
	/**
	 * Converts a value to JSON.
	 *
	 * @param value
	 *            the value, may be {@code null}
	 * @return the JSON text
	 * @throws IslandSerializationException
	 *             if the value cannot be converted
	 */
	String toJson(Object value);

	/**
	 * Converts JSON received from the client into an instance of the given type.
	 *
	 * @param <T>
	 *            the type to create
	 * @param json
	 *            the untrusted JSON text
	 * @param type
	 *            the type to create
	 * @return the value, {@code null} if the JSON is {@code null}
	 * @throws IslandSerializationException
	 *             if the JSON is malformed or does not match the type
	 */
	<T> T fromJson(String json, Class<T> type);
}
