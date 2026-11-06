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

import org.apache.wicket.util.lang.Args;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * An {@link IIslandSerializer} based on Jackson.
 * <p>
 * The default mapper rejects JSON properties the target type does not declare, so a client cannot
 * smuggle values past a request type, and leaves polymorphic type handling off. A custom mapper
 * passed to {@link #JacksonIslandSerializer(ObjectMapper)} must not enable default typing.
 *
 * @since 12.0.0
 */
public class JacksonIslandSerializer implements IIslandSerializer
{
	private final ObjectMapper mapper;

	/**
	 * Creates a serializer with the default mapper.
	 */
	public JacksonIslandSerializer()
	{
		this(JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build());
	}

	/**
	 * Creates a serializer with the given mapper.
	 *
	 * @param mapper
	 *            the mapper
	 */
	public JacksonIslandSerializer(ObjectMapper mapper)
	{
		this.mapper = Args.notNull(mapper, "mapper");
	}

	@Override
	public String toJson(Object value)
	{
		try
		{
			return mapper.writeValueAsString(value);
		}
		catch (JacksonException e)
		{
			throw new IslandSerializationException("Cannot convert a " +
				(value == null ? "null" : value.getClass().getName()) + " to JSON", e);
		}
	}

	@Override
	public <T> T fromJson(String json, Class<T> type)
	{
		Args.notNull(json, "json");
		Args.notNull(type, "type");

		try
		{
			return mapper.readValue(json, type);
		}
		catch (JacksonException e)
		{
			throw new IslandSerializationException("Cannot read a " + type.getName() + " from JSON",
				e);
		}
	}
}
