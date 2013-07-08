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
package org.apache.wicket.cdi.util.tester;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;

import org.apache.wicket.Application;
import org.apache.wicket.cdi.Auto;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.cdi.ConfigurationParameters;
import org.apache.wicket.cdi.IConversationPropagation;
import org.apache.wicket.cdi.IgnoreList;
import org.apache.wicket.cdi.Propagation;

/**
 * Specializes the CdiConfigration to allow the ConfigurationParams key to be
 * remapped after the Application is used to construct the WicketTester.
 * This is needed because WicketTester generates the ApplicationKey during construction
 * and does not contain a mechanism to override the name.  In the normal code the WicketFilter
 * sets the key to the filtername so remapping is not necessary.
 *
 * @author jsarman
 */
@ApplicationScoped
@Specializes
public class TestCdiConfiguration extends CdiConfiguration
{

	@PostConstruct
	@Override
	public void init()
	{
		super.init();
	}

	public
	@Produces
	@Propagation
	IConversationPropagation getPropagation()
	{
		return super.getPropagation();
	}


	public
	@Produces
	@Auto
	@Override
	Boolean isAutoConversationManagement()
	{
		return super.isAutoConversationManagement();
	}

	public
	@Produces
	@IgnoreList
	@Override
	String[] getPackagesToIgnore()
	{
		return super.getPackagesToIgnore();
	}

	public void remapApplicationKey(String key, Application app)
	{
		ConfigurationParameters cp = parameters.remove(key);
		parameters.put(app.getApplicationKey(), cp);
	}
}
