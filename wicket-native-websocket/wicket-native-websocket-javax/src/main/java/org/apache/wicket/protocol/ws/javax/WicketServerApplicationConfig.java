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
package org.apache.wicket.protocol.ws.javax;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Configures WicketServerEndpointConfig that will setup {@link org.apache.wicket.protocol.ws.javax.WicketEndpoint}
 * and a custom {@link javax.websocket.server.ServerEndpointConfig.Configurator} to collect the
 * useful information from the upgrade http request
 */
@SuppressWarnings("unused") // loaded with class-scan
public class WicketServerApplicationConfig implements ServerApplicationConfig
{
	@Override
	public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> classes)
	{
		Set<ServerEndpointConfig> configs = new HashSet<>();
		configs.add(new WicketServerEndpointConfig());
		return configs;
	}

	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> classes)
	{
		return Collections.emptySet();
	}
}
