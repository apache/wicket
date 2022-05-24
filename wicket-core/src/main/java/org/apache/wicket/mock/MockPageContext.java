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
package org.apache.wicket.mock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.wicket.MetaDataEntry;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.pageStore.IPageContext;

/**
 * Mock implementation of a page context - suitable for a single session only.
 */
public class MockPageContext implements IPageContext
{

	final String sessionId;

	MetaDataEntry<?>[] requestData;
	
	MetaDataEntry<?>[] sessionData;

	Map<String, Object> sessionAttributes = new HashMap<>();

	public MockPageContext()
	{
		this("dummy_id");
	}

	public MockPageContext(String sessionId)
	{
		this.sessionId = sessionId;
	}

	@Override
	public <T> T getRequestData(MetaDataKey<T> key, Supplier<T> defaultValue)
	{
		T value = key.get(requestData);
		if (value == null) {
			value = defaultValue.get();
			if (value != null) {
				requestData = key.set(requestData, value);
			}
		}

		return value;
	}

	@Override
	public <T extends Serializable> T getSessionAttribute(String key, Supplier<T> defaultValue)
	{
		@SuppressWarnings("unchecked")
		T value = (T)sessionAttributes.get(key);
		if (value == null && defaultValue != null) {
			value = defaultValue.get();
			sessionAttributes.put(key, value);
		}
		
		return value;
	}
	
	@Override
	public <T extends Serializable> T getSessionData(MetaDataKey<T> key, Supplier<T> defaultValue)
	{
		T value = key.get(sessionData);
		if (value == null && defaultValue != null) {
			value = defaultValue.get();
			sessionData = key.set(sessionData, value);
		}
		
		return value;
	}

	@Override
	public String getSessionId(boolean bind)
	{
		return sessionId;
	}

	public void clearRequest()
	{
		requestData = null;
	}
	
	public void clearSession() {
		sessionAttributes.clear();
		sessionData = null;
	}
}