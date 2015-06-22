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
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.strategies.page.AbstractPageAuthorizationStrategy;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.info.PageInfo;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTesterLazyIsPageStatelessRedirectToBufferTest.EmptyPage;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author pedrosans
 */
public class AbstractBookmarkableMapperTest extends WicketTestCase
{

	private static final int NOT_RENDERED_COUNT = 2;
	private static final int EXPIRED_ID = 2;

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{

			@Override
			protected void init()
			{
				super.init();

				getSecuritySettings().setAuthorizationStrategy(
					new AbstractPageAuthorizationStrategy()
					{
						@Override
						protected <T extends Page> boolean isPageAuthorized(Class<T> pageClass)
						{
							if (pageClass == EmptyPage.class)
							{
								throw new RestartResponseAtInterceptPageException(getHomePage());
							}
							return true;
						}
					});
			}
		};
	}
	
	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-4932">WICKET-4932</a>
	 */
	@Test(expected = PageExpiredException.class)
	public void itFailsToProcessAnExpiredPageIfShouldNotRecreateMountedPagesAfterExpiry()
	{
		tester.getApplication().getPageSettings().setRecreateBookmarkablePagesAfterExpiry(false);
		AbstractBookmarkableMapperStub mapper = new AbstractBookmarkableMapperStub();
		mapper.processHybrid(new PageInfo(EXPIRED_ID), MockPage.class, null, NOT_RENDERED_COUNT);
		Assert.fail("it shouldn't process expired pages if the app was flagged to not recreated mounted pages after expiry");
	}
	
	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-5734">WICKET-5734</a>
	 */
	@Test
	public void testProcessHybridWithAuthorizationException() throws Exception
	{
		AbstractBookmarkableMapperStub mapper = new AbstractBookmarkableMapperStub();
		mapper.processHybrid(new PageInfo(), EmptyPage.class, null, 0);
	}
	
	/** */
	public class AbstractBookmarkableMapperStub extends AbstractBookmarkableMapper
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
