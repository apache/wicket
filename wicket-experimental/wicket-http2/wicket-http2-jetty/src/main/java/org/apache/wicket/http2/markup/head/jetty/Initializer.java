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
package org.apache.wicket.http2.markup.head.jetty;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.apache.wicket.http2.Http2Settings;

/**
 * Initializes the jetty specific push builder API and makes it available through the HTTP2
 * settings
 */
public class Initializer implements IInitializer
{
	/**
	 * Initializes the push builder API of Jetty 9.3+
	 */
	@Override
	public void init(Application application)
	{
		Http2Settings http2Settings = Http2Settings.Holder.get(application);
		http2Settings.setPushBuilder(new Jetty9PushBuilder());
	}

	@Override
	public void destroy(Application application)
	{
		// NOOP
	}
}
