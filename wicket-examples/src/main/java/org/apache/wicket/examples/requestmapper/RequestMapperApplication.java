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

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Page;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.examples.requestmapper.packageMount.PackageMountedPage;
import org.apache.wicket.protocol.https.HttpsConfig;
import org.apache.wicket.protocol.https.HttpsMapper;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.core.request.mapper.MountedMapper;

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

		getRootRequestMapperAsCompound().add(new CustomHomeMapper(getHomePage()));

		getRootRequestMapperAsCompound().add(
			new LocaleFirstMapper(new MountedMapper("/localized", LocalizedPage.class)));

		mountPage("secured", HttpsPage.class);

		mountPackage("pMount", PackageMountedPage.class);

		mountResource("/print/${sheet}/${format}", new MapperDemoResourceReference());

		setRootRequestMapper(new HttpsMapper(getRootRequestMapper(), new LazyHttpsConfig()));
	}

	/**
	 * HttpsConfig that extracts the <i>http</i> port out of the current servlet request's local
	 * port. This way the demo can be used both with Jetty (port 8080) and at production (behind
	 * Apache proxy)
	 */
	private static class LazyHttpsConfig extends HttpsConfig
	{
		@Override
		public int getHttpPort()
		{
			int port = -1;

			RequestCycle requestCycle = RequestCycle.get();
			if (requestCycle != null)
			{
				HttpServletRequest containerRequest = (HttpServletRequest)requestCycle.getRequest()
					.getContainerRequest();
				if (containerRequest != null)
				{
					port = containerRequest.getLocalPort();
				}
			}

			if (port == -1)
			{
				port = super.getHttpPort();
			}

			return port;
		}

		@Override
		public int getHttpsPort()
		{
			return 443;
		}
	}
}
