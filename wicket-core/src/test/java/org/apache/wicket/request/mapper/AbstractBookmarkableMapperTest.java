package org.apache.wicket.request.mapper;

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

import org.apache.wicket.MockPage;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.info.PageInfo;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author pedrosans
 */
public class AbstractBookmarkableMapperTest extends WicketTestCase
{

	private static final int NOT_RENDERED_COUNT = 2;
	private static final int EXPIRED_ID = 2;


	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-4932">WICKET-4932</a>
	 */
	@Test(expected = PageExpiredException.class)
	public void itFailsToProcessAnExpiredPageIfShouldNotRecreateMountedPagesAfterExpiry()
	{
		tester.getApplication().getPageSettings().setRecreateMountedPagesAfterExpiry(false);
		AbstractBookmarkableMapperStub mapper = new AbstractBookmarkableMapperStub();
		mapper.processHybrid(new PageInfo(EXPIRED_ID), MockPage.class, null, NOT_RENDERED_COUNT);
		Assert.fail("it shouldn't process expired pages if the app was flagged to not recreated mounted pages after expiry");
	}

	/** only a stub since we are testing an abstract class */
	private static class AbstractBookmarkableMapperStub extends AbstractBookmarkableMapper
	{

		@Override
		protected UrlInfo parseRequest(Request request)
		{
			return null;
		}

		@Override
		protected Url buildUrl(UrlInfo info)
		{
			return null;
		}

		@Override
		protected boolean pageMustHaveBeenCreatedBookmarkable()
		{
			return false;
		}

		@Override
		public int getCompatibilityScore(Request request)
		{
			return 0;
		}

	}

}
