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

package org.apache.wicket.csp;

import static org.apache.wicket.csp.CSPDirective.DEFAULT_SRC;
import static org.apache.wicket.csp.CSPDirective.REPORT_URI;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

class CspReportUriTest
{
	@Test
	void developmentModeReportsViolationsBack()
	{
		Map<CSPDirective, List<CSPRenderable>> directives = blockingDirectives(
			RuntimeConfigurationType.DEVELOPMENT);

		assertTrue(directives.containsKey(REPORT_URI), directives.toString());
		assertTrue(directives.containsKey(DEFAULT_SRC), directives.toString());
	}

	@Test
	void deploymentModeDoesNotReportViolationsBack()
	{
		Map<CSPDirective, List<CSPRenderable>> directives = blockingDirectives(
			RuntimeConfigurationType.DEPLOYMENT);

		assertFalse(directives.containsKey(REPORT_URI), directives.toString());
		assertTrue(directives.containsKey(DEFAULT_SRC), directives.toString());
	}

	private static Map<CSPDirective, List<CSPRenderable>> blockingDirectives(
		RuntimeConfigurationType configurationType)
	{
		WicketTester tester = new WicketTester(new WebApplication()
		{
			@Override
			public Class<? extends Page> getHomePage()
			{
				return DummyHomePage.class;
			}

			@Override
			public RuntimeConfigurationType getConfigurationType()
			{
				return configurationType;
			}
		});
		try
		{
			return Map.copyOf(tester.getApplication().getCspSettings().blocking().getDirectives());
		}
		finally
		{
			tester.destroy();
		}
	}
}
