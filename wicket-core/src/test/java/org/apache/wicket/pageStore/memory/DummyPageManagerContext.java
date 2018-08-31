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
package org.apache.wicket.pageStore.memory;

import java.io.Serializable;

import org.apache.wicket.page.IPageManagerContext;

/**
 */
public class DummyPageManagerContext implements IPageManagerContext
{

	private Serializable attribute = null;
	private Object requestData;

	@Override
	public void setRequestData(Object data)
	{
		requestData = data;
	}

	@Override
	public Object getRequestData()
	{
		return requestData;
	}

	@Override
	public void setSessionAttribute(String key, Serializable value)
	{
		attribute = value;
	}

	@Override
	public Serializable getSessionAttribute(String key)
	{
		return attribute;
	}

	@Override
	public void bind()
	{
	}

	@Override
	public String getSessionId()
	{
		return "dummy_id";
	}

}