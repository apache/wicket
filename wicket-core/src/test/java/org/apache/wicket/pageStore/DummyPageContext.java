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
package org.apache.wicket.pageStore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.MetaDataEntry;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.pageStore.IPageContext;

/**
 * Dummy implementation of a page context - suitable for a single session only.
 */
public class DummyPageContext implements IPageContext
{

	final String sessionId;

	MetaDataEntry<?>[] requestData;
	
	MetaDataEntry<?>[] sessionData;

	Map<String, Object> sessionAttributes = new HashMap<>();

	public DummyPageContext()
	{
		this("dummy_id");
	}

	public DummyPageContext(String sessionId)
	{
		this.sessionId = sessionId;
	}

	@Override
	public <T> void setRequestData(MetaDataKey<T> key, T value)
	{
		requestData = key.set(requestData, value);
	}

	@Override
	public <T> T getRequestData(MetaDataKey<T> key)
	{
		return key.get(requestData);
	}

	@Override
	public <T extends Serializable> void setSessionAttribute(String key, T value)
	{
		sessionAttributes.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T getSessionAttribute(String key)
	{
		return (T)sessionAttributes.get(key);
	}
	
	@Override
	public <T extends Serializable> T setSessionData(MetaDataKey<T> key, T value)
	{
		sessionData = key.set(sessionData, value);
		
		return value;
	}

	@Override
	public <T extends Serializable> T getSessionData(MetaDataKey<T> key)
	{
		return key.get(sessionData);
	}

	@Override
	public void bind()
	{
	}

	@Override
	public String getSessionId()
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