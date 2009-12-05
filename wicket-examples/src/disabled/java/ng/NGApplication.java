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
package org.apache.wicket.examples.ng;

import org.apache.wicket.ng.markup.html.link.ILinkListener;
import org.apache.wicket.ng.protocol.http.WebApplication;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.listener.RequestListenerInterface;
import org.apache.wicket.ng.request.mapper.MountedMapper;

public class NGApplication extends WebApplication
{
	public NGApplication()
	{
		super();
	}

	@Override
	public void init()
	{
		mount(new MountedMapper("first-test-page", TestPage1.class));
		mount(new MountedMapper("third-test-page", TestPage3.class));
		mount(new MountedMapper("/page4/${color}/display", TestPage4.class));

		// load the interface
		RequestListenerInterface i = ILinkListener.INTERFACE;
	}

	@Override
	public Class<? extends RequestablePage> getHomePage()
	{
		return TestPage1.class;
	}
}
