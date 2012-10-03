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
package org.apache.wicket.util.string;

import org.apache.wicket.response.StringResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since
 */
public class JavaScriptUtilsTest extends Assert
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4546
	 * @throws Exception
	 */
	@Test
	public void writeJavaScriptUrl() throws Exception
	{
		StringResponse response = new StringResponse();
		String url = "some/url;jsessionid=1234?p1=v1&p2=v2";
		String id = "some&bad%id";
		boolean defer = true;
		String charset = "some&bad%%charset";
		JavaScriptUtils.writeJavaScriptUrl(response, url, id, defer, charset);

		assertEquals("<script type=\"text/javascript\" id=\"some&amp;bad%id\" defer=\"defer\" charset=\"some&amp;bad%%charset\" src=\"some/url;jsessionid=1234?p1=v1&p2=v2\"></script>\n", response.toString());
	}
}
