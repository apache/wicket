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

import java.util.List;
import java.util.Map;

/**
 * @since 6.0.0
 */
public final class JsonUtils
{
	private JsonUtils()
	{}

	/**
	 * Converts a Map to JSONArray suitable for jQuery#param().
	 *
	 * @param map
	 *      the map with key/value(s)
	 * @return a JSONArray that contains JSONObject's with name/value pairs
	 * @throws JSONException
	 */
	public static JSONArray asArray(Map<String, Object> map) throws JSONException
	{
		JSONArray jsonArray = new JSONArray();

		if (map != null)
		{
			for (Map.Entry<String, Object> entry : map.entrySet())
			{
				String name = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof List) {
					List<?> values = (List<?>) value;
					for (Object v : values)
					{
						if (v != null)
						{
							JSONObject object = new JSONObject();
							object.put("name", name);
							object.put("value", v);
							jsonArray.put(object);
						}
					}
				}
				else if (value != null)
				{
					if (value.getClass().isArray())
					{
						Object[] array = (Object[]) value;
						for (Object v : array)
						{
							if (v != null)
							{
								JSONObject object = new JSONObject();
								object.put("name", name);
								object.put("value", v);
								jsonArray.put(object);
							}
						}
					}
					else
					{
						JSONObject object = new JSONObject();
						object.put("name", name);
						object.put("value", value);
						jsonArray.put(object);
					}
				}
			}
		}

		return jsonArray;
	}
}
