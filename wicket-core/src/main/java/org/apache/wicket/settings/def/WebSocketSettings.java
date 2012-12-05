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
package org.apache.wicket.settings.def;

import java.util.concurrent.Executor;

import org.apache.wicket.settings.IWebSocketSettings;
import org.apache.wicket.util.lang.Args;

/**
 */
public class WebSocketSettings implements IWebSocketSettings
{
	/**
	 * The executor that handles the processing of websocket push message broadcasts.
	 */
	private Executor webSocketPushMessageExecutor = new SameThreadExecutor();

// ****************************************************************************
// IWebSocketSettings Implementation
// ****************************************************************************

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWebSocketPushMessageExecutor(Executor executor)
	{
		Args.notNull(executor, "executor");
		this.webSocketPushMessageExecutor = executor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Executor getWebSocketPushMessageExecutor()
	{
		return webSocketPushMessageExecutor;
	}

	/**
	 * Simple executor that runs the tasks in the caller thread.
	 */
	static final class SameThreadExecutor implements Executor {
		@Override
		public void execute(Runnable command)
		{
			command.run();
		}
	}
}
