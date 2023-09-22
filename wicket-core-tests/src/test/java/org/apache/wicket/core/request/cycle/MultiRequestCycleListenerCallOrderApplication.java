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
package org.apache.wicket.core.request.cycle;

import java.util.ArrayList;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

class MultiRequestCycleListenerCallOrderApplication extends WebApplication
{
	@Override
	public Class<MultiRequestCycleListenerCallOrderPage> getHomePage()
	{
		return MultiRequestCycleListenerCallOrderPage.class;
	}

	@Override
	public void init()
	{
		super.init();

		getRequestCycleListeners().add(new CallRecordingListener("first"));
		getRequestCycleListeners().add(new CallRecordingListener("second"));
	}

	public final ArrayList<String> callSequence = new ArrayList<String>();

	/**
	 * Records calls to each method in the {@code callSequence}.
	 */
	class CallRecordingListener implements IRequestCycleListener
	{
		private String name;

		CallRecordingListener(String name)
		{
			this.name = name;
		}

		@Override
		public void onBeginRequest(RequestCycle cycle)
		{
			callSequence.add(name + ".onBeginRequest");
		}

		@Override
		public void onRequestHandlerScheduled(final RequestCycle cycle, IRequestHandler handler)
		{
			if (handler != null)
				callSequence.add(name + ".onRequestHandlerScheduled");
		}

		@Override
		public void onRequestHandlerResolved(final RequestCycle cycle, IRequestHandler handler)
		{
			callSequence.add(name + ".onRequestHandlerResolved");
		}

		@Override
		public void onEndRequest(RequestCycle cycle)
		{
			callSequence.add(name + ".onEndRequest");
		}

		@Override
		public void onDetach(RequestCycle cycle)
		{
			callSequence.add(name + ".onDetach");
		}

		@Override
		public void onExceptionRequestHandlerResolved(final RequestCycle cycle,
			IRequestHandler handler, Exception exception)
		{
			callSequence.add(name + ".onExceptionRequestHandlerResolved");
		}

		@Override
		public IRequestHandler onException(RequestCycle cycle, Exception ex)
		{
			callSequence.add(name + ".onException");
			return null;
		}

		@Override
		public String toString()
		{
			return "Listener " + name;
		}

		@Override
		public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler)
		{
			callSequence.add(name + ".onRequestHandlerExecuted");
		}

		@Override
		public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url)
		{
		}
	}
}
