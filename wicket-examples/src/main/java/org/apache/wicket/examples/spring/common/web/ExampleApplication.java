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
package org.apache.wicket.examples.spring.common.web;

import org.apache.wicket.examples.spring.common.ContactDao;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

/**
 * Application class for our examples
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class ExampleApplication extends WebApplication
{

	/**
	 * this field holds a contact dao proxy that is safe to use in wicket components
	 */
	private ContactDao contactDaoProxy;

	/**
	 * this field holds the actual contact dao retrieved from spring context. this object should
	 * never be serialized because it will take the container with it, so BE CAREFUL when using
	 * this.
	 */
	private ContactDao contactDao;

	@Override
	protected void init()
	{
		getDebugSettings().setDevelopmentUtilitiesEnabled(true);

		// THIS LINE IS IMPORTANT - IT INSTALLS THE COMPONENT INJECTOR THAT WILL
		// INJECT NEWLY CREATED COMPONENTS WITH THEIR SPRING DEPENDENCIES
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));
	}

	@Override
	public Class getHomePage()
	{
		return HomePage.class;
	}

}
