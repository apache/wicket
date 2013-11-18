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
package org.apache.wicket.cdi.testapp;

import javax.inject.Inject;

import org.apache.wicket.Page;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * @author jsarman
 */
public class TestCdiApplication extends WebApplication
{

	@Inject
	@TestQualifier
	String testString;


	@Override
	public Class<? extends Page> getHomePage()
	{
		return TestPage.class;
	}

	@Override
	protected void init()
	{
		super.init();
		new CdiConfiguration().configure(this);
	}

	public String getInjectedTestString()
	{
		return testString;
	}

}
