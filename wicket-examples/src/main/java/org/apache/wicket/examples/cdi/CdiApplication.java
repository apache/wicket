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
package org.apache.wicket.examples.cdi;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.wicket.Page;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.jboss.weld.environment.servlet.Listener;

public class CdiApplication extends WebApplication
{

	@Override
	public Class<? extends Page> getHomePage()
	{
		return CdiHomePage.class;
	}

	@Override
	protected void init()
	{
		super.init();

		// lookup bean manager from Weld's servlet listener
		BeanManager manager = (BeanManager)getServletContext().getAttribute(
			Listener.BEAN_MANAGER_ATTRIBUTE_NAME);

		// configure wicket/cdi
		new CdiConfiguration(manager).configure(this);
	}

}
