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
package org.apache.wicket.util.collections;

import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("javadoc")
public class IntHashMapTest
{
	@SuppressWarnings({ "resource", "unchecked" })
	@Test
	public void serialize() throws IOException, ClassNotFoundException
	{
		IntHashMap<String> map = new IntHashMap<>();
		map.put(1, "one");
		map.put(2, "two");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(map);

		byte[] serialized = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
		ObjectInputStream ois = new ObjectInputStream(bais);
		IntHashMap<String> deserialized = (IntHashMap<String>) ois.readObject();
		assertNotNull(deserialized);
		assertEquals(deserialized.get(1), "one");
		assertEquals(deserialized.get(2), "two");

		// WICKET-5584
		deserialized.put(3, "three");

		// WICKET-5751
		deserialized.entrySet().iterator();
	}
}
