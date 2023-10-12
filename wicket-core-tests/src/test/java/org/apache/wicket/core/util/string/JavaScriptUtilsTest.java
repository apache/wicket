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
package org.apache.wicket.core.util.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.value.AttributeMap;
import org.junit.jupiter.api.Test;

/**
 * @since 1.5.7
 */
class JavaScriptUtilsTest
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4546
	 * 
	 * @throws Exception
	 */
	@Test
	public void writeJavaScript() throws Exception
	{
		AttributeMap attributes = new AttributeMap();
		attributes.putAttribute(JavaScriptUtils.ATTR_TYPE, "text/javascript");
		attributes.putAttribute(JavaScriptUtils.ATTR_ID, "some\"funny<id&%");
		attributes.putAttribute(JavaScriptUtils.ATTR_SCRIPT_DEFER, true);
		attributes.putAttribute("charset", "some\"funny<charset&%");
		attributes.putAttribute(JavaScriptUtils.ATTR_SCRIPT_SRC, "some/url;jsessionid=1234?p1=v1&p2=v2");
		StringResponse response = new StringResponse();
		JavaScriptUtils.writeScript(response, attributes);

		assertEquals(
				"<script type=\"text/javascript\" id=\"some&quot;funny&lt;id&amp;%\" defer=\"defer\" charset=\"some&quot;funny&lt;charset&amp;%\" src=\"some/url;jsessionid=1234?p1=v1&amp;p2=v2\"></script>\n",
			response.toString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5715
	 */
	@Test
	public void writeJavaScriptAsync()
	{
		AttributeMap attributes = new AttributeMap();
		attributes.putAttribute(JavaScriptUtils.ATTR_TYPE, "text/javascript");
		attributes.putAttribute(JavaScriptUtils.ATTR_ID, "some\"funny<id&%");
		attributes.putAttribute(JavaScriptUtils.ATTR_SCRIPT_DEFER, true);
		attributes.putAttribute(JavaScriptUtils.ATTR_SCRIPT_ASYNC, true);
		attributes.putAttribute("charset", "some\"funny<charset&%");
		attributes.putAttribute(JavaScriptUtils.ATTR_SCRIPT_SRC, "some/url;jsessionid=1234?p1=v1&p2=v2&p3=v3");
		StringResponse response = new StringResponse();
		JavaScriptUtils.writeScript(response, attributes);

		assertEquals(
				"<script type=\"text/javascript\" id=\"some&quot;funny&lt;id&amp;%\" defer=\"defer\" async=\"async\" charset=\"some&quot;funny&lt;charset&amp;%\" src=\"some/url;jsessionid=1234?p1=v1&amp;p2=v2&amp;p3=v3\"></script>\n",
				response.toString());
	}

	/**
	 */
	@Test
	public void writeInlineScript()
	{
		StringResponse response = new StringResponse();
		AttributeMap attributes = new AttributeMap();
		attributes.putAttribute(JavaScriptUtils.ATTR_TYPE, "text/javascript");
		JavaScriptUtils.writeInlineScript(response,
			"var message = 'Scripts are written to the <script></script> tag'", attributes);

		assertEquals("<script type=\"text/javascript\">\n" //
			+ "/*<![CDATA[*/\n" //
			+ "var message = 'Scripts are written to the <script><\\/script> tag'\n" //
			+ "/*]]>*/\n"//
			+ "</script>\n", response.toString());
	}

	/**
	 */
	@Test
	public void scriptTag()
	{
		assertEquals("<script type=\"text/javascript\">\n/*<![CDATA[*/\n",
			JavaScriptUtils.SCRIPT_OPEN_TAG);
		assertEquals("\n/*]]>*/\n</script>\n", JavaScriptUtils.SCRIPT_CLOSE_TAG);
	}
}
