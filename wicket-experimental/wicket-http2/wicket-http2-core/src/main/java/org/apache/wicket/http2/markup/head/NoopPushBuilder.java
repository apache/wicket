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
package org.apache.wicket.http2.markup.head;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class NoopPushBuilder implements PushBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(NoopPushBuilder.class);

	public static final NoopPushBuilder INSTANCE = new NoopPushBuilder();

	private NoopPushBuilder()
	{}

	@Override
	public void push(HttpServletRequest httpServletRequest, String... paths)
	{
		LOG.warn("This PushBuilder does nothing. Please use one of the other implementations - Jetty9 or Tomcat8.5+");
	}
}
