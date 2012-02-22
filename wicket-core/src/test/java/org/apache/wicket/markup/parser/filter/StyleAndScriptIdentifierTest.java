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
package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

/**
 * @since 6.0
 */
public class StyleAndScriptIdentifierTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4425
	 *
	 * Verifies that the content of <script id="script1" type="text/x-jquery-tmpl">
	 * wont be wrapped in CDATA, while all <script type="text/javascript"> will
	 * be wrapped unless they have their body already wrapped
	 *
	 * @throws Exception
	 */
	@Test
	public void doNotWrapScriptTemplates() throws Exception
	{
		executeTest(PageWithScriptTemplate.class, "PageWithScriptTemplate_expected.html");
	}

}
