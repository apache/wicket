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
package org.apache.wicket.islands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;

import tools.jackson.databind.json.JsonMapper;

/**
 * Base for island tests: an application with islands installed on the test build in
 * {@code testbuild/}, and helpers to call an island and read its answer.
 */
abstract class IslandTestCase extends WicketTestCase
{
	private static final Pattern DATA = Pattern.compile(
		"<data name=\"wicket-islands\"><!\\[CDATA\\[(.*?)\\]\\]></data>", Pattern.DOTALL);

	static final JsonMapper JSON = new JsonMapper();

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				IslandTestCase.this.configureIslands(IslandSettings.install(this));
			}

			@Override
			public RuntimeConfigurationType getConfigurationType()
			{
				return IslandTestCase.this.configurationType();
			}
		};
	}

	/**
	 * @param settings
	 *            the settings to configure
	 */
	protected void configureIslands(IslandSettings settings)
	{
		settings.setBasePath("org/apache/wicket/islands/testbuild/").setManifestFile("manifest.json");
	}

	/**
	 * @return the configuration type of the test application
	 */
	protected RuntimeConfigurationType configurationType()
	{
		return RuntimeConfigurationType.DEVELOPMENT;
	}

	/**
	 * Calls the island of the last rendered {@link IslandTestPage}.
	 *
	 * @return the island's answer
	 */
	Map<String, Object> call(String name, String callId, String payload)
	{
		IslandTestPage page = (IslandTestPage)tester.getLastRenderedPage();
		prepareCall(name, callId, payload, page.island.getKey());
		tester.executeBehavior(page.island.getBehaviors(IslandEndpointBehavior.class).get(0));
		return answer();
	}

	void prepareCall(String name, String callId, String payload, String key)
	{
		tester.getRequest().setMethod("POST");
		tester.getRequest().getPostParameters().setParameterValue(IslandEndpointBehavior.PARAM_KEY,
			key);
		tester.getRequest().getPostParameters().setParameterValue(IslandEndpointBehavior.PARAM_CALL,
			name);
		tester.getRequest().getPostParameters().setParameterValue(
			IslandEndpointBehavior.PARAM_CALL_ID, callId);
		if (payload != null)
		{
			tester.getRequest().getPostParameters().setParameterValue(
				IslandEndpointBehavior.PARAM_PAYLOAD, payload);
		}
	}

	/**
	 * @return the answer in the last response
	 */
	@SuppressWarnings("unchecked")
	Map<String, Object> answer()
	{
		String response = tester.getLastResponseAsString();
		Matcher matcher = DATA.matcher(response);
		assertTrue(matcher.find(), response);
		return JSON.readValue(matcher.group(1), Map.class);
	}

	@SuppressWarnings("unchecked")
	static Map<String, Object> error(Map<String, Object> answer)
	{
		assertEquals(Boolean.FALSE, answer.get("ok"));
		return (Map<String, Object>)answer.get("error");
	}
}
