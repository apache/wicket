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

import javax.inject.Inject;

import com.google.inject.Provider;
import org.apache.wicket.Component;

/**
 */
public class JavaxInjectTestComponent extends Component implements TestComponentInterface
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

	private final JavaxInjectTestNoComponent noComponent;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public JavaxInjectTestComponent(final String id)
	{
		super(id);
		noComponent = new JavaxInjectTestNoComponent();
		add(new JavaxInjectTestBehavior());
	}

	/**
	 * @return injectedField
	 */
	@Override
	public ITestService getInjectedField()
	{
		return injectedField;
	}

	/**
	 * @return injectedFieldBlue
	 */
	@Override
	public ITestService getInjectedFieldBlue()
	{
		return injectedFieldBlue;
	}

	/**
	 * @return injectedFieldRed
	 */
	@Override
	public ITestService getInjectedFieldRed()
	{
		return injectedFieldRed;
	}

	/**
	 * @return injectedFieldProvider
	 */
	@Override
	public Provider<ITestService> getInjectedFieldProvider()
	{
		return injectedFieldProvider;
	}

	/**
	 * Gets injectedOptionalField.
	 * 
	 * @return injectedOptionalField
	 */
	@Override
	public String getInjectedOptionalField()
	{
		return null;
	}

	/**
	 * @return injectedTypeLiteralField
	 */
	@Override
	public Map<String, String> getInjectedTypeLiteralField()
	{
		return injectedTypeLiteralField;
	}

	@Override
	protected void onRender()
	{
		// Do nothing.
	}

	/**
	 * @return String for noComponent
	 */
	public String getNoComponentString()
	{
		return noComponent.getString();
	}

}
