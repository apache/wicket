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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import org.apache.wicket.MockPage;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.handler.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.request.handler.PageAndComponentProvider;
import org.apache.wicket.request.mapper.info.PageInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author pedrosans
 */
public class AbstractBookmarkableMapperTest extends WicketTestCase
{

	private static final int NOT_RENDERED_COUNT = 2;
	private static final int EXPIRED_ID = 2;
	private AbstractBookmarkableMapperStub mapper;

	/** */
	@Before
	public void initialize()
	{
		mapper = new AbstractBookmarkableMapperStub();
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-4932">WICKET-4932</a>
	 */
	@Test(expected = PageExpiredException.class)
	public void itFailsToProcessAnExpiredPageIfShouldNotRecreateMountedPagesAfterExpiry()
	{
		tester.getApplication().getPageSettings().setRecreateMountedPagesAfterExpiry(false);
		mapper.processHybrid(new PageInfo(EXPIRED_ID), MockPage.class, null, NOT_RENDERED_COUNT);
		Assert.fail("it shouldn't process expired pages if the app was flagged to not recreated mounted pages after expiry");
	}

	/** */
	@Test
	public void itDoenstEndodesBookmarkableInfoForCallbacksInNonBookmarkablePages()
	{
		assertThat(mapper.mapHandler(anInterfaceHandlerFor(new NonBookmarkablePage(1))),
			nullValue());
		assertThat(mapper.mapHandler(anInterfaceHandlerFor(new BookmarkablePage())), notNullValue());
	}

	private ListenerInterfaceRequestHandler anInterfaceHandlerFor(MockPage page)
	{
		IRequestableComponent component = page.get("bar:foo");
		return new ListenerInterfaceRequestHandler(new PageAndComponentProvider(page, component),
			ILinkListener.INTERFACE);
	}

	/**
	 * An non bookmarkable page since there's no default constructor
	 */
	public static class NonBookmarkablePage extends MockPage
	{
		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * @param aMandatoryParameter
		 */
		public NonBookmarkablePage(int aMandatoryParameter)
		{
		}
	}

	/**
	 * An bookmarkable page since there's a default constructor
	 */
	public static class BookmarkablePage extends MockPage
	{
		/** */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * only a stub since we are testing an abstract class
	 */
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
			return new Url();
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

		@Override
		protected Url mapBookmarkableHandler(BookmarkableListenerInterfaceRequestHandler handler)
		{
			return super.mapBookmarkableHandler(handler);
		}

	}

}
