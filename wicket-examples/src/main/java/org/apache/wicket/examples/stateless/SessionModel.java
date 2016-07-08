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
package org.apache.wicket.examples.stateless;

import org.apache.wicket.Application;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Model that displays whether a session was created yet, and if it was, prints the session id.
 * 
 * @author Eelco Hillenius
 */
public class SessionModel implements IModel<String>
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getObject()
	{
		final String msg;
		String sessionId = Application.get()
			.getSessionStore()
			.getSessionId(RequestCycle.get().getRequest(), false);
		if (sessionId == null)
		{
			msg = "no concrete session is created yet (only a volatile one)";
		}
		else
		{
			msg = "a session exists for this client, with session id " + sessionId;
		}
		return msg;
	}

}
