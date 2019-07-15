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

import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.value.AttributeMap;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @since 1.5.7
 */
class CssUtilsTest
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4546
	 *
	 * @throws Exception
	 */
	@Test
	void writeLink() throws Exception
	{
		StringResponse response = new StringResponse();
		String url = "some/url;jsessionid=1234?with=parameters&p1=v1";
		String media = "some&bad&media";
		AttributeMap attributes = new AttributeMap(Collections.singleton(CssUtils.ATTR_LINK_HREF));
		attributes.add(CssUtils.ATTR_LINK_REL, "stylesheet");
		attributes.add(CssUtils.ATTR_TYPE, "text/css");
		attributes.add(CssUtils.ATTR_LINK_HREF, url);
		attributes.add(CssUtils.ATTR_LINK_MEDIA, media);
		attributes.add(CssUtils.ATTR_ID, "markupId");
		CssUtils.writeLink(response, attributes);

		assertEquals("<link rel=\"stylesheet\" type=\"text/css\" href=\"some/url;jsessionid=1234?with=parameters&p1=v1\" media=\"some&amp;bad&amp;media\" id=\"markupId\"/>", response.toString());
	}
}
