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
package org.apache.wicket.cdi.testapp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

import org.apache.wicket.Session;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;

/**
 * An application that reports whether CDI was applied to it.
 */
public class TestFilteredApplication extends MockApplication
{
	@Inject
	TestAppScope appScope;

	private boolean postConstructed;

	private boolean preDestroyed;

	@PostConstruct
	void onPostConstruct()
	{
		postConstructed = true;
	}

	@PreDestroy
	void onPreDestroy()
	{
		preDestroyed = true;
	}

	@Override
	public Session newSession(Request request, Response response)
	{
		return new TestFilteredSession(request);
	}

	public boolean isInjected()
	{
		return appScope != null;
	}

	public boolean isPostConstructed()
	{
		return postConstructed;
	}

	public boolean isPreDestroyed()
	{
		return preDestroyed;
	}
}
