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
package org.apache.wicket.protocol.http;

import java.util.List;

import org.apache.wicket.request.IRequestHandler;

// TODO (NG) DO something useful here
public class DummyRequestLogger implements IRequestLogger
{

	public DummyRequestLogger()
	{
	}

	public int getCurrentActiveRequestCount()
	{
		return 0;
	}

	public SessionData[] getLiveSessions()
	{
		return null;
	}

	public int getPeakSessions()
	{
		return 0;
	}

	public List<RequestData> getRequests()
	{
		return null;
	}

	public int getTotalCreatedSessions()
	{
		return 0;
	}

	public void logEventTarget(IRequestHandler target)
	{
	}

	public void logResponseTarget(IRequestHandler target)
	{
	}

	public void objectCreated(Object value)
	{
	}

	public void objectRemoved(Object value)
	{
	}

	public void objectUpdated(Object value)
	{
	}

	public void requestTime(long timeTaken)
	{
	}

	public void sessionCreated(String id)
	{
	}

	public void sessionDestroyed(String sessionId)
	{
	}

}
