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
package org.apache.wicket.settings.def;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.ResourceSettings;
import org.apache.wicket.util.time.Duration;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for https://issues.apache.org/jira/browse/WICKET-5625
 */
public class SetCustomResourceSettingsTest extends WicketTestCase
{
	final String expected = "~!@";
	ResourceSettings resSettings;

	@Override
	@Before
	public void commonBefore() {
		resSettings = mock(ResourceSettings.class);
		when(resSettings.getParentFolderPlaceholder()).thenReturn(expected);

		super.commonBefore();
	}

	@Test
	public void check() {
		ResourceSettings settings = tester.getApplication().getResourceSettings();
		assertThat(settings.getParentFolderPlaceholder(), is(equalTo(expected)));
	}

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication() {
			@Override
			protected void init()
			{
				super.init();
				setResourceSettings(resSettings);
			}
		};
	}
}
