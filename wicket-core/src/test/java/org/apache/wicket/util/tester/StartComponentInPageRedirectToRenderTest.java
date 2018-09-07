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
package org.apache.wicket.util.tester;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.RequestCycleSettings;
import org.junit.jupiter.api.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-5679
 */
class StartComponentInPageRedirectToRenderTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				getRequestCycleSettings().setRenderStrategy(RequestCycleSettings.RenderStrategy.REDIRECT_TO_RENDER);
			}
		};
	}

	@Test
    void startComponentInPageRedirectToRender()
	{
		tester.startComponentInPage(new Label("foo"));
	}
}
