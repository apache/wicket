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
package org.apache.wicket.guice;

import org.apache.wicket.Component;
import org.apache.wicket.markup.MarkupStream;

import com.google.inject.Inject;

public class TestComponent extends Component
{
	private static final long serialVersionUID = 1L;

	@Inject
	private ITestService injectedField;

	@Inject
	@Red
	private ITestService injectedFieldRed;
	
	@Inject
	@Blue
	private ITestService injectedFieldBlue;

	@Inject
	public void injectService(ITestService service)
	{
		injectedMethod = service;
	}
	
	@Inject
	public void injectServiceRed(@Red ITestService service)
	{
		injectedMethodRed = service;
	}

	@Inject
	public void injectServiceBlue(@Blue ITestService service)
	{
		injectedMethodBlue = service;
	}
	
	private ITestService injectedMethod, injectedMethodRed, injectedMethodBlue;
	
	public TestComponent(String id)
	{
		super(id);
	}

	@Override
	protected void onRender(MarkupStream markupStream)
	{
		// Do nothing.
	}

	public ITestService getInjectedField()
	{
		return injectedField;
	}

	public ITestService getInjectedFieldRed()
	{
		return injectedFieldRed;
	}

	public ITestService getInjectedFieldBlue()
	{
		return injectedFieldBlue;
	}

	public ITestService getInjectedMethod()
	{
		return injectedMethod;
	}
	
	public ITestService getInjectedMethodRed()
	{
		return injectedMethodRed;
	}

	public ITestService getInjectedMethodBlue()
	{
		return injectedMethodBlue;
	}
}
