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
package org.apache.wicket.metrics.aspects.session;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;


/**
 * Listener that counts the current active sessions
 * 
 * @author Tobias Soloschenko
 *
 */
@WebListener
public class SessionCountListener implements HttpSessionListener
{

	@Override
	public void sessionDestroyed(HttpSessionEvent event)
	{
		dec(event);
	}

	@Override
	public void sessionCreated(HttpSessionEvent event)
	{
		inc(event);
	}

	/**
	 * Used to wire an aspect around
	 * 
	 * @param event the http session event
	 */
	public void dec(HttpSessionEvent event)
	{
		// NOOP for aspect usage
	}
	
	/**
	 * Used to wire an aspect around
	 * 
	 * @param event the http session event
	 */
	public void inc(HttpSessionEvent event)
	{
		// NOOP for aspect usage
	}
}
