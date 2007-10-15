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
package org.apache.wicket.quickstart;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Application object for your web application. If you want to run this application without
 * deploying, run the Start class.
 * 
 * @see org.apache.wicket.quickstart.Start#main(String[])
 */
public class QuickStartApplication extends WebApplication
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(QuickStartApplication.class);

	/**
	 * Constructor
	 */
	public QuickStartApplication()
	{
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Index.class;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#newSession(Request, Response)
	 */
	public Session newSession(Request request, Response response)
	{
		return new QuickStartSession(QuickStartApplication.this, request);
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#init()
	 */
	protected void init()
	{
		// put any configuration here
	}
}