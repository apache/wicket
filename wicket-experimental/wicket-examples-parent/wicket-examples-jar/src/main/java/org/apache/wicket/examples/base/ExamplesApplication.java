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
package org.apache.wicket.examples.base;

import org.apache.wicket.Page;
import org.apache.wicket.examples.basic.BasicExamplesPage;
import org.apache.wicket.examples.basic.guestbook.GuestbookPage;
import org.apache.wicket.examples.basic.helloworld.HelloWorldPage;
import org.apache.wicket.examples.basic.linkcounter.LinkCounterPage;
import org.apache.wicket.examples.components.ComponentExamplesPage;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Application object for your web application.
 */
public class ExamplesApplication extends WebApplication
{
	@Override
	public void init()
	{
		super.init();

		mountPage("/basic", BasicExamplesPage.class);
		mountPage("/basic/helloworld", HelloWorldPage.class);
		mountPage("/basic/linkcounter", LinkCounterPage.class);
		mountPage("/basic/guestbook", GuestbookPage.class);

		mountPage("/components", ComponentExamplesPage.class);
	}

	@Override
	public Class<? extends Page> getHomePage()
	{
		return HomePage.class;
	}
}
