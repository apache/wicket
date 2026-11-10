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
package org.apache.wicket.islands;

import java.util.List;

import org.apache.wicket.islands.serializer.IIslandSerializer;
import org.apache.wicket.islands.serializer.IslandSerializationException;

/**
 * Builds the answer to an island call: the value of the data entry the client runtime reads.
 */
final class IslandAnswer
{
	static final String DATA_NAME = "wicket-islands";

	static final String EXPIRED_MESSAGE = "The page has expired or changed. Reload it to continue.";

	private IslandAnswer()
	{
	}

	/**
	 * @param callId
	 *            the id of the call, {@code null} for a page-less call, which is answered directly
	 * @throws IllegalStateException
	 *             if the value cannot be serialized, which is a fault of the server, not the client
	 */
	static String success(IIslandSerializer serializer, String callId, Object value,
		List<String> invalidations)
	{
		String json;
		try
		{
			json = serializer.toJson(value);
		}
		catch (IslandSerializationException e)
		{
			throw new IllegalStateException("The result of the call cannot be serialized", e);
		}
		return answer(serializer, callId, "\"ok\":true,\"value\":" + json, invalidations);
	}

	static String failure(IIslandSerializer serializer, String callId, String kind,
		List<String> messages, List<String> invalidations)
	{
		return answer(serializer, callId,
			"\"ok\":false,\"error\":{\"kind\":" + serializer.toJson(kind) + ",\"messages\":" +
				serializer.toJson(messages) + "}",
			invalidations);
	}

	/**
	 * @return an answer that only carries invalidations, for requests that are no island call
	 */
	static String invalidations(IIslandSerializer serializer, List<String> invalidations)
	{
		return "{\"invalidate\":" + serializer.toJson(invalidations) + "}";
	}

	/**
	 * @param state
	 *            the JSON of the form state
	 * @return an answer that carries the form state of a form island, for its peer
	 */
	static String formState(IIslandSerializer serializer, String key, String state)
	{
		return "{\"form\":{\"key\":" + serializer.toJson(key) + ",\"state\":" + state + "}}";
	}

	private static String answer(IIslandSerializer serializer, String callId, String outcome,
		List<String> invalidations)
	{
		String call = callId == null ? "" : "\"call\":" + serializer.toJson(callId) + ",";
		return "{" + call + outcome + ",\"invalidate\":" + serializer.toJson(invalidations) + "}";
	}
}
