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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

/**
 * Tests for {@link JacksonIslandSerializer}.
 */
class JacksonIslandSerializerTest
{
	public record Point(int x, int y)
	{
	}

	public enum Color
	{
		RED
	}

	/**
	 * A class Jackson would write through its getters, like an entity.
	 */
	public static class Customer
	{
		public String getName()
		{
			return "Ann";
		}

		public String getPasswordHash()
		{
			return "secret";
		}
	}

	public record Order(Customer customer)
	{
	}

	public record Everything(List<Point> points, Map<String, Integer> counts, Color color,
		LocalDate day, UUID id, Optional<String> none)
	{
	}

	private final JacksonIslandSerializer serializer = new JacksonIslandSerializer();

	@Test
	void roundTripsRecords()
	{
		assertEquals(new Point(1, 2), serializer.fromJson(serializer.toJson(new Point(1, 2)),
			Point.class));
	}

	@Test
	void writesRecordsCollectionsMapsEnumsAndSingleValues()
	{
		assertEquals("{\"points\":[{\"x\":1,\"y\":2}],\"counts\":{\"a\":1},\"color\":\"RED\"," +
			"\"day\":\"2026-11-10\",\"id\":\"00000000-0000-0001-0000-000000000002\",\"none\":null}",
			serializer.toJson(new Everything(List.of(new Point(1, 2)), Map.of("a", 1), Color.RED,
				LocalDate.of(2026, 11, 10), new UUID(1, 2), Optional.empty())));
	}

	@Test
	void refusesClassesWrittenThroughTheirGetters()
	{
		IslandSerializationException e = assertThrows(IslandSerializationException.class,
			() -> serializer.toJson(new Customer()));
		assertTrue(e.getCause().getMessage().contains(Customer.class.getName()),
			e.getCause().getMessage());
		assertThrows(IslandSerializationException.class,
			() -> serializer.toJson(new Order(new Customer())));
		assertThrows(IslandSerializationException.class,
			() -> serializer.toJson(List.of(new Customer())));
	}

	@Test
	void customMapperDecidesForItself()
	{
		assertTrue(new JacksonIslandSerializer(JsonMapper.builder().build())
			.toJson(new Customer())
			.contains("secret"));
	}

	@Test
	void rejectsUnknownProperties()
	{
		assertThrows(IslandSerializationException.class,
			() -> serializer.fromJson("{\"x\":1,\"y\":2,\"z\":3}", Point.class));
	}

	@Test
	void rejectsMalformedJson()
	{
		assertThrows(IslandSerializationException.class,
			() -> serializer.fromJson("{\"x\":", Point.class));
	}

	@Test
	void doesNotHonourTypeInformationInTheInput()
	{
		assertThrows(IslandSerializationException.class, () -> serializer
			.fromJson("{\"@class\":\"java.util.ArrayList\",\"x\":1,\"y\":2}", Point.class));
	}
}
