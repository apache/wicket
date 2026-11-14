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
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.ValueSerializerModifier;
import tools.jackson.databind.ser.bean.BeanSerializerBase;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * An {@link IIslandSerializer} based on Jackson.
 * <p>
 * The default mapper writes only records, collections, maps, arrays, enums and values Jackson
 * writes as a single value, such as strings, numbers, dates and types with a {@code @JsonValue}.
 * Any other class is refused, because Jackson would write every getter it has: handing an entity
 * to an island would publish all its fields to the browser and could load its lazy relations. Map
 * such objects to records made for the client.
 * <p>
 * The default mapper also rejects JSON properties the target type does not declare, so a client
 * cannot smuggle values past a request type, and leaves polymorphic type handling off. A custom
 * mapper passed to {@link #JacksonIslandSerializer(ObjectMapper)} must not enable default typing;
 * start from {@link #defaultMapperBuilder()} to keep the rules above.
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
		this(defaultMapperBuilder().build());
	}

	/**
	 * @return a builder of the default mapper, to add modules or configuration to
	 */
	public static JsonMapper.Builder defaultMapperBuilder()
	{
		return JsonMapper.builder()
			.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.addModule(new SimpleModule("wicket-islands-records-only")
				.setSerializerModifier(new RecordsOnly()));
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

	/**
	 * Refuses classes Jackson would write through their getters, unless they are records.
	 */
	private static final class RecordsOnly extends ValueSerializerModifier
	{
		private static final long serialVersionUID = 1L;

		@Override
		public ValueSerializer<?> modifySerializer(SerializationConfig config,
			BeanDescription.Supplier beanDesc, ValueSerializer<?> serializer)
		{
			if (serializer instanceof BeanSerializerBase bean && !bean.handledType().isRecord())
			{
				return new Refused(bean.handledType());
			}
			return serializer;
		}
	}

	private static final class Refused extends StdSerializer<Object>
	{
		private Refused(Class<?> type)
		{
			super(type);
		}

		@Override
		public void serialize(Object value, JsonGenerator generator, SerializationContext context)
		{
			context.reportBadDefinition(handledType(), "Islands only write records, collections, " +
				"maps, arrays, enums and single values, not a " + handledType().getName() +
				": map it to a record, or configure the island serializer with a mapper of your " +
				"own");
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
