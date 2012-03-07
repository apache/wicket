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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.MockPage;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.core.request.mapper.AbstractBookmarkableMapper.UrlInfo;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link UrlInfo}
 */
public class UrlInfoTest extends Assert
{

	/**
	 * WICKET-4038 & WICKET-4054
	 */
	@Test
	public void wicket4038()
	{
		PageParameters parameters = new PageParameters();
		parameters.add(WebRequest.PARAM_AJAX, "true");
		parameters.add(WebRequest.PARAM_AJAX_BASE_URL, "base/url");
		parameters.add(WebRequest.PARAM_AJAX_REQUEST_ANTI_CACHE, "12345.6879");

		AbstractBookmarkableMapper.UrlInfo info = new UrlInfo(null, MockPage.class, parameters);
		assertNull(info.getPageParameters());
	}
}
