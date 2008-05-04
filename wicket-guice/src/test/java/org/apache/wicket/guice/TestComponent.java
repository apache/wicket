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

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.MarkupStream;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TestComponent extends Component<Void>
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
	private Provider<ITestService> injectedFieldProvider;

	@Inject
	private Map<String, String> injectedTypeLiteralField;

	private ITestService injectedMethod, injectedMethodRed, injectedMethodBlue;

	private Provider<ITestService> injectedMethodProvider;

	private Map<String, String> injectedTypeLiteralMethod;

	public TestComponent(String id)
	{
		super(id);
	}

	public ITestService getInjectedField()
	{
		return injectedField;
	}

	public ITestService getInjectedFieldBlue()
	{
		return injectedFieldBlue;
	}

	public ITestService getInjectedFieldRed()
	{
		return injectedFieldRed;
	}

	public ITestService getInjectedMethod()
	{
		return injectedMethod;
	}

	public ITestService getInjectedMethodBlue()
	{
		return injectedMethodBlue;
	}

	public ITestService getInjectedMethodRed()
	{
		return injectedMethodRed;
	}

	public Provider<ITestService> getInjectedFieldProvider()
	{
		return injectedFieldProvider;
	}

	public Map<String, String> getInjectedTypeLiteralField()
	{
		return injectedTypeLiteralField;
	}

	public Provider<ITestService> getInjectedMethodProvider()
	{
		return injectedMethodProvider;
	}

	public Map<String, String> getInjectedTypeLiteralMethod()
	{
		return injectedTypeLiteralMethod;
	}

	@Inject
	public void injectProvider(Provider<ITestService> provider)
	{
		injectedMethodProvider = provider;
	}

	@Inject
	public void injectService(ITestService service)
	{
		injectedMethod = service;
	}

	@Inject
	public void injectServiceBlue(@Blue ITestService service)
	{
		injectedMethodBlue = service;
	}

	@Inject
	public void injectServiceRed(@Red ITestService service)
	{
		injectedMethodRed = service;
	}

	@Inject
	public void injectTypeLiteral(Map<String, String> map)
	{
		injectedTypeLiteralMethod = map;
	}

	@Override
	protected void onRender(MarkupStream markupStream)
	{
		// Do nothing.
	}
}
