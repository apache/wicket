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
package org.apache.wicket.ajax.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @since 6.0.0
 */
public class JsonUtilsTest extends Assert
{
	@Test
	public void asArray() throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("string", "stringValue");
		assertEquals("[{\"name\":\"string\",\"value\":\"stringValue\"}]", JsonUtils.asArray(map).toString());

		map.clear();
		map.put("int", 1);
		assertEquals("[{\"name\":\"int\",\"value\":1}]", JsonUtils.asArray(map).toString());

		map.clear();
		map.put("boolean", true);
		assertEquals("[{\"name\":\"boolean\",\"value\":true}]", JsonUtils.asArray(map).toString());

		map.clear();
		List<Object> listValues = new ArrayList<Object>();
		listValues.addAll(Arrays.asList("listValue", 2, false, null));
		map.put("list", listValues);
		assertEquals("[{\"name\":\"list\",\"value\":\"listValue\"},{\"name\":\"list\",\"value\":2},{\"name\":\"list\",\"value\":false}]", JsonUtils.asArray(map).toString());

		map.clear();
		Object[] arrayValues = {"arrayValue", 3, null, true};
		map.put("array", arrayValues);
		assertEquals("[{\"name\":\"array\",\"value\":\"arrayValue\"},{\"name\":\"array\",\"value\":3},{\"name\":\"array\",\"value\":true}]", JsonUtils.asArray(map).toString());
	}
}
