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
package org.apache.wicket.atmosphere.tester;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AsyncSupport;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResponse;

/**
 *
 */
public class TesterAsyncSupport<E extends AtmosphereResource> implements AsyncSupport<E>
{
	@Override
	public String getContainerName()
	{
		return "wicket-atmosphere-tester";
	}

	@Override
	public void init(ServletConfig sc) throws ServletException
	{

	}

	@Override
	public Action service(AtmosphereRequest req, AtmosphereResponse res) throws IOException, ServletException
	{
		return Action.CONTINUE;
	}

	@Override
	public void action(E actionEvent)
	{
		System.err.println("TesterEventSupport#action(): " + actionEvent);
	}

	@Override
	public boolean supportWebSocket()
	{
		return true;
	}

	@Override
	public AsyncSupport complete(E r)
	{
		return this;
	}
}
