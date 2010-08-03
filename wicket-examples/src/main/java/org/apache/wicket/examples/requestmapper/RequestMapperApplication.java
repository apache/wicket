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
package org.apache.wicket.examples.requestmapper;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.protocol.https.HttpsConfig;
import org.apache.wicket.protocol.https.HttpsMapper;
import org.apache.wicket.request.mapper.MountedMapper;

/**
 * @author mgrigorov
 */
public class RequestMapperApplication extends WicketExampleApplication
{

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return RequestMapperHomePage.class;
	}

	/**
	 * 
	 * @see org.apache.wicket.examples.WicketExampleApplication#init()
	 */
	@Override
	public void init()
	{
		super.init();

		getRootRequestMapperAsCompound().add(new CustomHomeMapper());

		getRootRequestMapperAsCompound().add(
			new LocaleFirstMapper(new MountedMapper("/localized", LocalizedPage.class)));

		getRootRequestMapperAsCompound().add(new MountedMapper("secured", HttpsPage.class));

		setRootRequestMapper(new HttpsMapper(getRootRequestMapper(), new HttpsConfig()));
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#getConfigurationType()
	 */
	@Override
	public String getConfigurationType()
	{
		return Application.DEVELOPMENT;
	}


}
